package online.yueyun.message.service;

import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;

/**
 * 消息发送器接口
 * 每个消息渠道对应一个发送器实现
 * 
 * @author yueyun
 */
public interface MessageSender {

    /**
     * 获取渠道类型
     *
     * @return 渠道类型
     */
    MessageChannelEnum getChannel();

    /**
     * 发送消息
     *
     * @param message 消息对象
     * @return 发送结果
     */
    MessageResult send(Message message);

    /**
     * 检查消息是否有效
     *
     * @param message 消息对象
     * @return 是否有效
     */
    boolean validate(Message message);

    /**
     * 判断当前渠道是否可用
     *
     * @return 是否可用
     */
    boolean isEnabled();
} 