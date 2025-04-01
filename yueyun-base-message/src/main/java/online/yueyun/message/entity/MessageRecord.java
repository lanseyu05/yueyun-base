package online.yueyun.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.enums.MessageStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息记录实体
 */
@Data
@TableName("message_record")
public class MessageRecord {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息幂等ID
     */
    private String messageId;

    /**
     * 消息渠道
     */
    private MessageChannelEnum channel;

    /**
     * 消息状态
     */
    private MessageStatusEnum status;

    /**
     * 消息模板ID
     */
    private String templateId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收者列表（JSON数组）
     */
    private String receivers;

    /**
     * 抄送者列表（JSON数组）
     */
    private String ccList;

    /**
     * 密送者列表（JSON数组）
     */
    private String bccList;

    /**
     * 附件列表（JSON数组）
     */
    private String attachments;

    /**
     * 模板参数（JSON对象）
     */
    private String params;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
} 