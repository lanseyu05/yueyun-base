package online.yueyun.message.sender;

import online.yueyun.message.dto.MessageRequest;

/**
 * 消息发送器接口
 */
public interface MessageSender {
    /**
     * 发送消息
     *
     * @param request 消息请求
     * @return 是否发送成功
     */
    boolean send(MessageRequest request);

    /**
     * 获取消息渠道
     *
     * @return 消息渠道
     */
    String getChannel();
} 