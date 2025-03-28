package online.yueyun.message.enums;

import lombok.Getter;

/**
 * 消息渠道枚举
 *
 * @author yueyun
 */
@Getter
public enum MessageChannelEnum {

    /**
     * 邮件
     */
    EMAIL("email", "电子邮件"),

    /**
     * 短信
     */
    SMS("sms", "手机短信"),

    /**
     * 飞书
     */
    FEISHU("feishu", "飞书"),

    /**
     * 钉钉
     */
    DINGTALK("dingtalk", "钉钉");

    /**
     * 渠道编码
     */
    private final String code;

    /**
     * 渠道描述
     */
    private final String desc;

    MessageChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取渠道类型
     *
     * @param code 渠道编码
     * @return 渠道类型枚举
     */
    public static MessageChannelEnum getByCode(String code) {
        for (MessageChannelEnum channelEnum : values()) {
            if (channelEnum.getCode().equals(code)) {
                return channelEnum;
            }
        }
        return EMAIL; // 默认使用邮件
    }
} 