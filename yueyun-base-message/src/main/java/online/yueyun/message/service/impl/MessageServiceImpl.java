package online.yueyun.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.dto.MessageRequest;
import online.yueyun.message.entity.MessageRecord;
import online.yueyun.message.entity.MessageTemplate;
import online.yueyun.message.enums.MessageStatusEnum;
import online.yueyun.message.factory.MessageSenderFactory;
import online.yueyun.message.mapper.MessageRecordMapper;
import online.yueyun.message.mapper.MessageTemplateMapper;
import online.yueyun.message.sender.MessageSender;
import online.yueyun.message.service.MessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageSenderFactory messageSenderFactory;
    private final MessageRecordMapper messageRecordMapper;
    private final MessageTemplateMapper messageTemplateMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    
    @Qualifier("messageTaskExecutor")
    private final Executor messageTaskExecutor;
    
    /**
     * 消息幂等性过期时间（秒）
     */
    private static final long MESSAGE_IDEMPOTENT_EXPIRE = 60 * 60; // 1小时
    
    /**
     * 待重试消息锁过期时间（秒）
     */
    private static final long RETRY_LOCK_EXPIRE = 30;
    
    /**
     * 消息重试间隔（毫秒）
     */
    private static final long[] RETRY_INTERVALS = {
            5 * 1000,      // 第1次重试：5秒后
            30 * 1000,     // 第2次重试：30秒后
            60 * 1000,     // 第3次重试：1分钟后
            5 * 60 * 1000, // 第4次重试：5分钟后
            30 * 60 * 1000 // 第5次重试：30分钟后
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(MessageRequest request) {
        // 生成消息ID
        String messageId = generateMessageId(request);
        
        // 检查幂等性
        if (checkMessageIdempotent(messageId)) {
            log.info("消息已存在，跳过处理，幂等ID: {}", messageId);
            return getExistingRecordId(messageId);
        }
        
        try {
            // 设置幂等标记
            setMessageIdempotent(messageId);
            
            // 验证消息模板
            if (request.getTemplateId() != null) {
                validateMessageTemplate(request);
            }
            
            // 创建消息记录
            MessageRecord record = createMessageRecord(request);
            record.setMessageId(messageId); // 设置幂等ID
            
            // 保存消息记录
            messageRecordMapper.insert(record);
            
            // 获取消息发送器
            MessageSender sender = messageSenderFactory.getMessageSender(request.getChannel());
            
            // 更新消息状态为发送中
            record.setStatus(MessageStatusEnum.SENDING);
            messageRecordMapper.updateById(record);
            
            // 发送消息
            boolean success = sender.send(request);
            
            // 更新消息状态
            record.setStatus(success ? MessageStatusEnum.SUCCESS : MessageStatusEnum.FAILED);
            if (success) {
                record.setSendTime(LocalDateTime.now());
            } else {
                record.setErrorMessage("消息发送失败");
                // 设置下次重试时间
                if (record.getRetryCount() < record.getMaxRetryCount()) {
                    record.setNextRetryTime(calculateNextRetryTime(record.getRetryCount()));
                }
            }
            
            // 更新消息记录
            messageRecordMapper.updateById(record);
            
            return record.getId();
        } catch (Exception e) {
            log.error("发送消息异常", e);
            
            // 获取现有记录
            MessageRecord record = getRecordByMessageId(messageId);
            if (record != null) {
                // 更新消息状态为失败
                record.setStatus(MessageStatusEnum.FAILED);
                record.setErrorMessage(e.getMessage());
                // 设置下次重试时间
                if (record.getRetryCount() < record.getMaxRetryCount()) {
                    record.setNextRetryTime(calculateNextRetryTime(record.getRetryCount()));
                }
                messageRecordMapper.updateById(record);
            }
            
            throw new RuntimeException("发送消息异常: " + e.getMessage(), e);
        }
    }

    @Override
    public Long sendMessageAsync(MessageRequest request) {
        // 生成消息ID
        String messageId = generateMessageId(request);
        
        // 检查幂等性
        if (checkMessageIdempotent(messageId)) {
            log.info("消息已存在，跳过处理，幂等ID: {}", messageId);
            return getExistingRecordId(messageId);
        }
        
        // 设置幂等标记
        setMessageIdempotent(messageId);
        
        // 验证消息模板
        if (request.getTemplateId() != null) {
            validateMessageTemplate(request);
        }
        
        // 创建消息记录
        MessageRecord record = createMessageRecord(request);
        record.setMessageId(messageId); // 设置幂等ID
        
        // 保存消息记录
        messageRecordMapper.insert(record);
        
        // 异步发送消息
        CompletableFuture.runAsync(() -> processMessageAsync(record.getId(), request), messageTaskExecutor);
        
        return record.getId();
    }
    
    /**
     * 异步处理消息发送
     */
    private void processMessageAsync(Long recordId, MessageRequest request) {
        try {
            // 获取消息记录
            MessageRecord record = messageRecordMapper.selectById(recordId);
            if (record == null) {
                log.error("消息记录不存在，ID: {}", recordId);
                return;
            }
            
            // 获取消息发送器
            MessageSender sender = messageSenderFactory.getMessageSender(record.getChannel());
            
            // 更新消息状态为发送中
            record.setStatus(MessageStatusEnum.SENDING);
            messageRecordMapper.updateById(record);
            
            // 发送消息
            boolean success = sender.send(request);
            
            // 更新消息状态
            record.setStatus(success ? MessageStatusEnum.SUCCESS : MessageStatusEnum.FAILED);
            record.setUpdateTime(LocalDateTime.now());
            
            if (success) {
                record.setSendTime(LocalDateTime.now());
            } else {
                record.setErrorMessage("消息发送失败");
                // 设置下次重试时间
                if (record.getRetryCount() < record.getMaxRetryCount()) {
                    record.setNextRetryTime(calculateNextRetryTime(record.getRetryCount()));
                }
            }
            
            // 更新消息记录
            messageRecordMapper.updateById(record);
            
            log.info("异步发送消息完成，ID: {}, 接收人: {}, 状态: {}", 
                    record.getId(), 
                    record.getReceivers(), 
                    record.getStatus().getDesc());
            
        } catch (Exception e) {
            log.error("异步发送消息异常，ID: {}", recordId, e);
            
            // 获取消息记录
            MessageRecord record = messageRecordMapper.selectById(recordId);
            if (record == null) {
                return;
            }
            
            // 更新消息状态为失败
            record.setStatus(MessageStatusEnum.FAILED);
            record.setErrorMessage(e.getMessage());
            record.setUpdateTime(LocalDateTime.now());
            // 设置下次重试时间
            if (record.getRetryCount() < record.getMaxRetryCount()) {
                record.setNextRetryTime(calculateNextRetryTime(record.getRetryCount()));
            }
            messageRecordMapper.updateById(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retryMessage(Long messageId) {
        // 查询消息记录
        MessageRecord record = messageRecordMapper.selectById(messageId);
        if (record == null) {
            log.error("消息记录不存在，ID: {}", messageId);
            return false;
        }
        
        // 检查重试次数
        if (record.getRetryCount() >= record.getMaxRetryCount()) {
            log.error("消息已达到最大重试次数，ID: {}", messageId);
            return false;
        }
        
        return doRetryMessage(record);
    }
    
    /**
     * 执行消息重试
     */
    private boolean doRetryMessage(MessageRecord record) {
        try {
            // 构建消息请求
            MessageRequest request = buildMessageRequest(record);
            
            // 获取消息发送器
            MessageSender sender = messageSenderFactory.getMessageSender(record.getChannel());
            
            // 更新消息状态为发送中
            record.setStatus(MessageStatusEnum.SENDING);
            record.setUpdateTime(LocalDateTime.now());
            messageRecordMapper.updateById(record);
            
            // 发送消息
            boolean success = sender.send(request);
            
            // 更新消息状态和重试次数
            record.setStatus(success ? MessageStatusEnum.SUCCESS : MessageStatusEnum.FAILED);
            record.setRetryCount(record.getRetryCount() + 1);
            record.setUpdateTime(LocalDateTime.now());
            
            if (success) {
                record.setSendTime(LocalDateTime.now());
                record.setNextRetryTime(null); // 成功后清空下次重试时间
            } else {
                record.setErrorMessage("重试发送失败");
                // 如果还可以重试，设置下次重试时间
                if (record.getRetryCount() < record.getMaxRetryCount()) {
                    record.setNextRetryTime(calculateNextRetryTime(record.getRetryCount()));
                }
            }
            
            // 更新消息记录
            messageRecordMapper.updateById(record);
            
            log.info("重试发送消息完成，ID: {}, 接收人: {}, 状态: {}, 重试次数: {}/{}", 
                    record.getId(), 
                    record.getReceivers(), 
                    record.getStatus().getDesc(),
                    record.getRetryCount(),
                    record.getMaxRetryCount());
            
            return success;
        } catch (Exception e) {
            log.error("重试发送消息异常", e);
            
            // 更新消息状态为失败
            record.setStatus(MessageStatusEnum.FAILED);
            record.setErrorMessage(e.getMessage());
            record.setRetryCount(record.getRetryCount() + 1);
            record.setUpdateTime(LocalDateTime.now());
            
            // 如果还可以重试，设置下次重试时间
            if (record.getRetryCount() < record.getMaxRetryCount()) {
                record.setNextRetryTime(calculateNextRetryTime(record.getRetryCount()));
            }
            
            messageRecordMapper.updateById(record);
            
            return false;
        }
    }
    
    /**
     * 计算下次重试时间
     */
    private LocalDateTime calculateNextRetryTime(Integer retryCount) {
        long interval = retryCount < RETRY_INTERVALS.length ? 
                RETRY_INTERVALS[retryCount] : RETRY_INTERVALS[RETRY_INTERVALS.length - 1];
        return LocalDateTime.now().plusSeconds(interval / 1000);
    }
    
    /**
     * 定时扫描失败消息进行重试（每分钟执行一次）
     */
    @Scheduled(fixedRate = 60000)
    public void scanFailedMessages() {
        log.info("开始扫描失败消息进行重试");
        
        // 获取需要重试的消息记录
        List<MessageRecord> failedRecords = getFailedMessagesForRetry();
        
        for (MessageRecord record : failedRecords) {
            // 获取分布式锁，防止重复处理
            String lockKey = "message:retry:lock:" + record.getId();
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", RETRY_LOCK_EXPIRE, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(locked)) {
                try {
                    log.info("准备重试消息，ID: {}, 重试次数: {}/{}", 
                            record.getId(), record.getRetryCount(), record.getMaxRetryCount());
                    
                    // 异步执行重试
                    CompletableFuture.runAsync(() -> doRetryMessage(record), messageTaskExecutor);
                    
                } catch (Exception e) {
                    log.error("处理重试消息异常，ID: {}", record.getId(), e);
                }
            }
        }
    }
    
    /**
     * 获取需要重试的失败消息
     */
    private List<MessageRecord> getFailedMessagesForRetry() {
        LambdaQueryWrapper<MessageRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageRecord::getStatus, MessageStatusEnum.FAILED)
                .lt(MessageRecord::getRetryCount, RETRY_INTERVALS.length)
                .le(MessageRecord::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(MessageRecord::getNextRetryTime)
                .last("LIMIT 100"); // 限制每次处理的数量
        
        return messageRecordMapper.selectList(queryWrapper);
    }

    @Override
    public String getMessageStatus(Long messageId) {
        MessageRecord record = messageRecordMapper.selectById(messageId);
        if (record == null) {
            return null;
        }
        return record.getStatus().getCode();
    }
    
    /**
     * 生成消息幂等ID
     */
    private String generateMessageId(MessageRequest request) {
        try {
            String content = request.getContent();
            String receivers = String.join(",", request.getReceivers());
            String channel = request.getChannel().getCode();
            String templateId = request.getTemplateId();
            
            // 使用消息特征生成唯一ID
            String messageKey = channel + ":" + templateId + ":" + receivers + ":" + 
                    (content != null ? content.hashCode() : "") + ":" +
                    (request.getParams() != null ? objectMapper.writeValueAsString(request.getParams()).hashCode() : "");
            
            return UUID.nameUUIDFromBytes(messageKey.getBytes()).toString();
        } catch (Exception e) {
            log.warn("生成消息ID异常，使用随机UUID", e);
            return UUID.randomUUID().toString();
        }
    }
    
    /**
     * 检查消息幂等性
     */
    private boolean checkMessageIdempotent(String messageId) {
        String key = "message:idempotent:" + messageId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 设置消息幂等标记
     */
    private void setMessageIdempotent(String messageId) {
        String key = "message:idempotent:" + messageId;
        redisTemplate.opsForValue().set(key, "1", MESSAGE_IDEMPOTENT_EXPIRE, TimeUnit.SECONDS);
    }
    
    /**
     * 获取已存在的消息记录ID
     */
    private Long getExistingRecordId(String messageId) {
        MessageRecord record = getRecordByMessageId(messageId);
        return record != null ? record.getId() : null;
    }
    
    /**
     * 根据消息ID获取记录
     */
    private MessageRecord getRecordByMessageId(String messageId) {
        LambdaQueryWrapper<MessageRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageRecord::getMessageId, messageId);
        return messageRecordMapper.selectOne(queryWrapper);
    }
    
    /**
     * 验证消息模板
     */
    private void validateMessageTemplate(MessageRequest request) {
        MessageTemplate template = messageTemplateMapper.selectOne(
                new LambdaQueryWrapper<MessageTemplate>()
                    .eq(MessageTemplate::getCode, request.getTemplateId())
                    .eq(MessageTemplate::getChannel, request.getChannel())
                    .eq(MessageTemplate::getEnabled, true)
        );
        
        if (template == null) {
            throw new IllegalArgumentException("消息模板不存在或未启用: " + request.getTemplateId());
        }
        
        // 如果请求中没有内容，则使用模板内容
        if (request.getContent() == null) {
            String content = template.getContent();
            
            // 如果有参数，则替换模板变量
            if (request.getParams() != null && !request.getParams().isEmpty()) {
                for (Map.Entry<String, Object> entry : request.getParams().entrySet()) {
                    content = content.replace("${" + entry.getKey() + "}", 
                            entry.getValue() != null ? entry.getValue().toString() : "");
                }
            }
            
            request.setContent(content);
        }
    }
    
    /**
     * 创建消息记录
     */
    private MessageRecord createMessageRecord(MessageRequest request) {
        MessageRecord record = new MessageRecord();
        record.setChannel(request.getChannel());
        record.setStatus(MessageStatusEnum.PENDING);
        record.setTemplateId(request.getTemplateId());
        record.setTitle(request.getTitle());
        record.setContent(request.getContent());
        
        try {
            record.setReceivers(objectMapper.writeValueAsString(request.getReceivers()));
            
            if (request.getCcList() != null && !request.getCcList().isEmpty()) {
                record.setCcList(objectMapper.writeValueAsString(request.getCcList()));
            }
            
            if (request.getBccList() != null && !request.getBccList().isEmpty()) {
                record.setBccList(objectMapper.writeValueAsString(request.getBccList()));
            }
            
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                record.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
            }
            
            if (request.getParams() != null && !request.getParams().isEmpty()) {
                record.setParams(objectMapper.writeValueAsString(request.getParams()));
            }
        } catch (JsonProcessingException e) {
            log.error("序列化消息记录字段异常", e);
        }
        
        record.setRetryCount(0);
        record.setMaxRetryCount(request.getMaxRetryCount());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        return record;
    }
    
    /**
     * 构建消息请求
     */
    private MessageRequest buildMessageRequest(MessageRecord record) {
        MessageRequest request = new MessageRequest();
        request.setChannel(record.getChannel());
        request.setTemplateId(record.getTemplateId());
        request.setTitle(record.getTitle());
        request.setContent(record.getContent());
        
        try {
            // 反序列化JSON字符串为Java对象
            if (record.getReceivers() != null) {
                request.setReceivers(objectMapper.readValue(record.getReceivers(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
            
            if (record.getCcList() != null) {
                request.setCcList(objectMapper.readValue(record.getCcList(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
            
            if (record.getBccList() != null) {
                request.setBccList(objectMapper.readValue(record.getBccList(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
            
            if (record.getParams() != null) {
                request.setParams(objectMapper.readValue(record.getParams(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)));
            }
            
            if (record.getAttachments() != null) {
                request.setAttachments(objectMapper.readValue(record.getAttachments(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, MessageRequest.Attachment.class)));
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化消息记录字段异常", e);
        }
        
        request.setMaxRetryCount(record.getMaxRetryCount());
        
        return request;
    }
} 