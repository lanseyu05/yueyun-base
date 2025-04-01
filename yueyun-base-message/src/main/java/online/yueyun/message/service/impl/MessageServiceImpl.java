package online.yueyun.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    @Override
    public Long sendMessage(MessageRequest request) {
        // 创建消息记录
        MessageRecord record = createMessageRecord(request);
        
        // 保存消息记录
        messageRecordMapper.insert(record);
        
        try {
            // 获取消息发送器
            MessageSender sender = messageSenderFactory.getMessageSender(request.getChannel());
            
            // 发送消息
            boolean success = sender.send(request);
            
            // 更新消息状态
            record.setStatus(success ? MessageStatusEnum.SUCCESS : MessageStatusEnum.FAILED);
            if (success) {
                record.setSendTime(LocalDateTime.now());
            }
            
            // 更新消息记录
            messageRecordMapper.updateById(record);
            
            return record.getId();
        } catch (Exception e) {
            log.error("发送消息异常", e);
            
            // 更新消息状态为失败
            record.setStatus(MessageStatusEnum.FAILED);
            record.setErrorMessage(e.getMessage());
            messageRecordMapper.updateById(record);
            
            return record.getId();
        }
    }

    @Override
    @Async
    public Long sendMessageAsync(MessageRequest request) {
        // 创建消息记录
        MessageRecord record = createMessageRecord(request);
        
        // 保存消息记录
        messageRecordMapper.insert(record);
        
        // 异步发送消息
        CompletableFuture.runAsync(() -> {
            try {
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
                }
                
                // 更新消息记录
                messageRecordMapper.updateById(record);
            } catch (Exception e) {
                log.error("异步发送消息异常", e);
                
                // 更新消息状态为失败
                record.setStatus(MessageStatusEnum.FAILED);
                record.setErrorMessage(e.getMessage());
                messageRecordMapper.updateById(record);
            }
        });
        
        return record.getId();
    }

    @Override
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
        
        try {
            // 构建消息请求
            MessageRequest request = buildMessageRequest(record);
            
            // 获取消息发送器
            MessageSender sender = messageSenderFactory.getMessageSender(record.getChannel());
            
            // 发送消息
            boolean success = sender.send(request);
            
            // 更新消息状态和重试次数
            record.setStatus(success ? MessageStatusEnum.SUCCESS : MessageStatusEnum.FAILED);
            record.setRetryCount(record.getRetryCount() + 1);
            if (success) {
                record.setSendTime(LocalDateTime.now());
            }
            
            // 更新消息记录
            messageRecordMapper.updateById(record);
            
            return success;
        } catch (Exception e) {
            log.error("重试发送消息异常", e);
            
            // 更新消息状态为失败
            record.setStatus(MessageStatusEnum.FAILED);
            record.setErrorMessage(e.getMessage());
            record.setRetryCount(record.getRetryCount() + 1);
            messageRecordMapper.updateById(record);
            
            return false;
        }
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
     * 创建消息记录
     */
    private MessageRecord createMessageRecord(MessageRequest request) {
        MessageRecord record = new MessageRecord();
        record.setChannel(request.getChannel());
        record.setStatus(MessageStatusEnum.PENDING);
        record.setTemplateId(request.getTemplateId());
        record.setTitle(request.getTitle());
        record.setContent(request.getContent());
        record.setReceivers(convertListToJson(request.getReceivers()));
        
        if (request.getCcList() != null && !request.getCcList().isEmpty()) {
            record.setCcList(convertListToJson(request.getCcList()));
        }
        
        if (request.getBccList() != null && !request.getBccList().isEmpty()) {
            record.setBccList(convertListToJson(request.getBccList()));
        }
        
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            record.setAttachments(convertObjectToJson(request.getAttachments()));
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
        
        // 这里需要反序列化JSON字符串为Java对象，实际应使用Jackson或Gson等库
        // 这里简化处理，仅做示例
        request.setReceivers(java.util.Arrays.asList(record.getReceivers().replaceAll("[\\[\\]\"]", "").split(",")));
        
        return request;
    }
    
    /**
     * 将列表转换为JSON字符串
     */
    private String convertListToJson(java.util.List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return list.toString();
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    private String convertObjectToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
} 