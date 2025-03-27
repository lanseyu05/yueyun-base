package online.yueyun.mq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息记录实体类，用于跟踪消息的发送状态和消费状态
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecord {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 业务键
     */
    private String businessKey;
    
    /**
     * 消息主题
     */
    private String topic;
    
    /**
     * 消息标签（RocketMQ专用）
     */
    private String tag;
    
    /**
     * 消息内容（JSON格式）
     */
    private String content;
    
    /**
     * 消息状态：0-待发送，1-已发送，2-发送失败，3-已消费，4-消费失败
     */
    private Integer status;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 消费组
     */
    private String consumerGroup;
    
    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 消息状态枚举
     */
    public enum Status {
        /**
         * 待发送
         */
        PENDING(0),
        
        /**
         * 已发送
         */
        SENT(1),
        
        /**
         * 发送失败
         */
        SEND_FAILED(2),
        
        /**
         * 已消费
         */
        CONSUMED(3),
        
        /**
         * 消费失败
         */
        CONSUME_FAILED(4);
        
        private final int value;
        
        Status(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
} 