package online.yueyun.message.service;

import online.yueyun.message.dto.MessageRequest;

/**
 * 消息服务接口
 */
public interface MessageService {
    /**
     * 发送消息
     *
     * @param request 消息请求
     * @return 消息ID
     */
    Long sendMessage(MessageRequest request);

    /**
     * 异步发送消息
     *
     * @param request 消息请求
     * @return 消息ID
     */
    Long sendMessageAsync(MessageRequest request);

    /**
     * 重试发送消息
     *
     * @param messageId 消息ID
     * @return 是否重试成功
     */
    boolean retryMessage(Long messageId);

    /**
     * 获取消息状态
     *
     * @param messageId 消息ID
     * @return 消息状态
     */
    String getMessageStatus(Long messageId);
} 