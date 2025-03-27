package online.yueyun.mq.service;

import online.yueyun.mq.model.MessageRecord;

import java.util.List;

/**
 * 消息记录服务接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface MessageRecordService {
    
    /**
     * 保存消息记录
     *
     * @param record 消息记录
     * @return 是否保存成功
     */
    boolean save(MessageRecord record);
    
    /**
     * 更新消息状态
     *
     * @param msgId 消息ID
     * @param status 消息状态
     * @return 是否更新成功
     */
    boolean updateStatus(String msgId, MessageRecord.Status status);
    
    /**
     * 更新消息记录
     *
     * @param record 消息记录
     * @return 是否更新成功
     */
    boolean update(MessageRecord record);
    
    /**
     * 根据消息ID查询消息记录
     *
     * @param msgId 消息ID
     * @return 消息记录
     */
    MessageRecord getByMsgId(String msgId);
    
    /**
     * 根据业务键查询消息记录
     *
     * @param businessKey 业务键
     * @return 消息记录
     */
    MessageRecord getByBusinessKey(String businessKey);
    
    /**
     * 查询需要重试发送的消息
     *
     * @param limit 查询数量
     * @return 消息记录列表
     */
    List<MessageRecord> findToRetry(int limit);
    
    /**
     * 查询已发送但未确认消费的消息
     *
     * @param topic 主题
     * @param consumerGroup 消费组
     * @param limit 查询数量
     * @return 消息记录列表
     */
    List<MessageRecord> findSentButNotConsumed(String topic, String consumerGroup, int limit);
    
    /**
     * 标记消息为已消费
     *
     * @param msgId 消息ID
     * @param consumerGroup 消费组
     * @return 是否标记成功
     */
    boolean markAsConsumed(String msgId, String consumerGroup);
    
    /**
     * 标记消息为消费失败
     *
     * @param msgId 消息ID
     * @param consumerGroup 消费组
     * @param errorMessage 错误信息
     * @return 是否标记成功
     */
    boolean markAsConsumedFailed(String msgId, String consumerGroup, String errorMessage);
    
    /**
     * 检查消息是否已经被消费
     *
     * @param msgId 消息ID
     * @param consumerGroup 消费组
     * @return 是否已被消费
     */
    boolean isConsumed(String msgId, String consumerGroup);
} 