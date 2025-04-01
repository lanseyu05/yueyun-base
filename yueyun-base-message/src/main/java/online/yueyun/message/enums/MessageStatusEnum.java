package online.yueyun.message.enums;

import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
public enum MessageStatusEnum {
    PENDING("pending", "待发送"),
    SENDING("sending", "发送中"),
    SUCCESS("success", "发送成功"),
    FAILED("failed", "发送失败");

    private final String code;
    private final String desc;

    MessageStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 