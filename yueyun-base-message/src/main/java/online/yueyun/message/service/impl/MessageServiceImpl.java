package online.yueyun.message.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.MessageProperties;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.enums.MessageTypeEnum;
import online.yueyun.message.exception.MessageException;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import online.yueyun.message.service.MessageSender;
import online.yueyun.message.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 * 
 * @author yueyun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageProperties messageProperties;
    private final List<MessageSender> messageSenders;

    @Override
    public MessageResult send(Message message) {
        // 验证消息基本信息
        validateMessage(message);
        
        // 确保消息ID存在
        ensureMessageId(message);
        
        // 如果没有指定渠道，使用默认渠道
        if (message.getChannel() == null) {
            message.setChannel(getDefaultChannel());
        }
        
        // 获取消息发送器
        MessageSender sender = getSender(message.getChannel());
        
        if (sender == null) {
            throw new MessageException("未找到渠道类型为 " + message.getChannel().getDesc() + " 的消息发送器");
        }
        
        // 检查发送器是否可用
        if (!sender.isEnabled()) {
            throw new MessageException("渠道类型为 " + message.getChannel().getDesc() + " 的消息发送器未启用");
        }
        
        // 发送消息
        return sender.send(message);
    }

    @Override
    public List<MessageResult> batchSend(List<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }
        
        // 批量发送
        return messages.stream()
                .map(this::send)
                .collect(Collectors.toList());
    }

    @Override
    @Async("messageTaskExecutor")
    public String sendAsync(Message message) {
        // 验证消息基本信息
        validateMessage(message);
        
        // 确保消息ID存在
        ensureMessageId(message);
        
        // 记录异步发送请求
        log.info("异步发送消息: messageId={}, channel={}, receivers={}",
                message.getMessageId(), 
                message.getChannel() != null ? message.getChannel().getCode() : getDefaultChannel().getCode(),
                message.getReceivers());
        
        // 异步发送
        CompletableFuture.runAsync(() -> {
            try {
                send(message);
            } catch (Exception e) {
                log.error("异步消息发送失败: messageId={}, error={}", 
                        message.getMessageId(), e.getMessage(), e);
            }
        });
        
        return message.getMessageId();
    }

    @Override
    public MessageResult sendWithChannel(Message message, MessageChannelEnum channel) {
        // 设置渠道并发送
        message.setChannel(channel);
        return send(message);
    }

    @Override
    public MessageResult sendWithTemplate(String templateId, Map<String, Object> params, List<String> receivers) {
        return sendWithTemplate(templateId, params, receivers, getDefaultChannel());
    }

    @Override
    public MessageResult sendWithTemplate(String templateId, Map<String, Object> params, List<String> receivers, MessageChannelEnum channel) {
        // 构建消息
        Message message = Message.builder()
                .templateId(templateId)
                .templateParams(params)
                .receivers(receivers)
                .channel(channel)
                .type(MessageTypeEnum.NOTIFICATION)
                .sendTime(new Date())
                .build();
        
        return send(message);
    }
    
    /**
     * 验证消息基本信息
     *
     * @param message 消息对象
     */
    private void validateMessage(Message message) {
        if (message == null) {
            throw new MessageException("消息对象不能为空");
        }
        
        if (CollectionUtils.isEmpty(message.getReceivers())) {
            throw new MessageException("消息接收人不能为空");
        }
    }
    
    /**
     * 确保消息ID存在
     *
     * @param message 消息对象
     */
    private void ensureMessageId(Message message) {
        if (StringUtils.isBlank(message.getMessageId())) {
            message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
    }
    
    /**
     * 获取默认消息渠道
     *
     * @return 默认渠道
     */
    private MessageChannelEnum getDefaultChannel() {
        String defaultChannels = messageProperties.getDefaultChannels();
        if (StringUtils.isBlank(defaultChannels)) {
            return MessageChannelEnum.EMAIL;
        }
        
        // 支持配置多个默认渠道，用逗号分隔，取第一个可用的渠道
        String[] channels = defaultChannels.split(",");
        for (String channel : channels) {
            MessageChannelEnum channelEnum = MessageChannelEnum.getByCode(channel.trim());
            MessageSender sender = getSender(channelEnum);
            
            if (sender != null && sender.isEnabled()) {
                return channelEnum;
            }
        }
        
        return MessageChannelEnum.EMAIL;
    }
    
    /**
     * 根据渠道类型获取消息发送器
     *
     * @param channel 渠道类型
     * @return 消息发送器
     */
    private MessageSender getSender(MessageChannelEnum channel) {
        return messageSenders.stream()
                .filter(sender -> sender.getChannel() == channel)
                .findFirst()
                .orElse(null);
    }
} 