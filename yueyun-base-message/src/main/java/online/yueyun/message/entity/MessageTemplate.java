package online.yueyun.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import online.yueyun.common.entity.BaseEntity;
import online.yueyun.message.enums.MessageChannelEnum;

/**
 * 消息模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_template")
public class MessageTemplate extends BaseEntity {
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
} 