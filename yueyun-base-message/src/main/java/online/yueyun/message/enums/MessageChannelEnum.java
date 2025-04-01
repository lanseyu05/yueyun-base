package online.yueyun.message.enums;

import lombok.Getter;

/**
 * 消息渠道枚举
 */
@Getter
public enum MessageChannelEnum {
    EMAIL("email", "邮件"),
    SMS("sms", "短信"),
    DINGTALK("dingtalk", "钉钉"),
    WECHAT("wechat", "微信");

    private final String code;
    private final String desc;

    MessageChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 