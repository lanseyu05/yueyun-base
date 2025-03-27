package online.yueyun.mq.service;

/**
 * 消息消费记录服务接口，用于幂等性控制
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface MessageConsumedService {
    
    /**
     * 记录消息已消费
     *
     * @param msgId 消息ID
     * @param consumerGroup 消费组
     * @return 是否成功
     */
    boolean markAsConsumed(String msgId, String consumerGroup);
    
    /**
     * 检查消息是否已消费
     *
     * @param msgId 消息ID
     * @param consumerGroup 消费组
     * @return 是否已消费
     */
    boolean isConsumed(String msgId, String consumerGroup);
} 