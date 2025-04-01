package online.yueyun.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import online.yueyun.message.enums.MessageChannelEnum;

import java.time.LocalDateTime;

/**
 * 消息模板实体
 */
@Data
@TableName("message_template")
public class MessageTemplate {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码
     */
    private String code;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 消息渠道
     */
    private MessageChannelEnum channel;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板参数说明（JSON格式）
     */
    private String params;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 