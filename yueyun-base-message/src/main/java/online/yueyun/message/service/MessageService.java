package online.yueyun.message.service;

import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;

import java.util.List;
import java.util.Map;

/**
 * 消息服务接口
 * 
 * @author yueyun
 */
public interface MessageService {

    /**
     * 发送消息
     *
     * @param message 消息对象
     * @return 发送结果
     */
    MessageResult send(Message message);

    /**
     * 批量发送消息
     *
     * @param messages 消息列表
     * @return 发送结果列表
     */
    List<MessageResult> batchSend(List<Message> messages);

    /**
     * 异步发送消息
     *
     * @param message 消息对象
     * @return 消息ID
     */
    String sendAsync(Message message);

    /**
     * 使用指定渠道发送消息
     *
     * @param message 消息对象
     * @param channel 渠道类型
     * @return 发送结果
     */
    MessageResult sendWithChannel(Message message, MessageChannelEnum channel);

    /**
     * 使用模板发送消息
     *
     * @param templateId 模板ID
     * @param params 模板参数
     * @param receivers 接收人列表
     * @return 发送结果
     */
    MessageResult sendWithTemplate(String templateId, Map<String, Object> params, List<String> receivers);

    /**
     * 使用模板通过指定渠道发送消息
     *
     * @param templateId 模板ID
     * @param params 模板参数
     * @param receivers 接收人列表
     * @param channel 渠道类型
     * @return 发送结果
     */
    MessageResult sendWithTemplate(String templateId, Map<String, Object> params, List<String> receivers, MessageChannelEnum channel);
} 