package online.yueyun.mq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息消费记录实体类，用于实现幂等性控制
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageConsumed {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 消费组
     */
    private String consumerGroup;
    
    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;
} 