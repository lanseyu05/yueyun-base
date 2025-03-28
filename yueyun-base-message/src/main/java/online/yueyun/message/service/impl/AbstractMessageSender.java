package online.yueyun.message.service.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.exception.MessageException;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import online.yueyun.message.service.MessageSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.UUID;

/**
 * 抽象消息发送器
 * 提供基本验证和公共方法
 * 
 * @author yueyun
 */
@Slf4j
public abstract class AbstractMessageSender implements MessageSender {

    @Override
    public MessageResult send(Message message) {
        // 检查消息是否有效
        if (!validate(message)) {
            return MessageResult.failure(message.getMessageId(), "消息参数无效");
        }
        
        // 确保消息ID存在
        ensureMessageId(message);
        
        try {
            // 调用子类实现的实际发送方法
            return doSend(message);
        } catch (Exception e) {
            log.error("消息发送失败, channel={}, messageId={}, error={}",
                    getChannel().getCode(), message.getMessageId(), e.getMessage(), e);
            return MessageResult.failure(message.getMessageId(), "消息发送失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validate(Message message) {
        if (message == null) {
            return false;
        }
        
        // 验证接收人列表
        if (CollectionUtils.isEmpty(message.getReceivers())) {
            log.warn("消息接收人列表为空");
            return false;
        }
        
        return validateChannelSpecific(message);
    }
    
    /**
     * 确保消息ID存在
     *
     * @param message 消息对象
     */
    protected void ensureMessageId(Message message) {
        if (StringUtils.isBlank(message.getMessageId())) {
            message.setMessageId(generateMessageId());
        }
    }
    
    /**
     * 生成消息ID
     *
     * @return 消息ID
     */
    protected String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 子类需要实现的实际发送方法
     *
     * @param message 消息对象
     * @return 发送结果
     * @throws MessageException 消息异常
     */
    protected abstract MessageResult doSend(Message message) throws MessageException;
    
    /**
     * 子类需要实现的渠道特定验证
     *
     * @param message 消息对象
     * @return 是否有效
     */
    protected abstract boolean validateChannelSpecific(Message message);
} 