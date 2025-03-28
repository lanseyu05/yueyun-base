package online.yueyun.message.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 * 
 * @author yueyun
 */
@Getter
public enum MessageTypeEnum {
    
    /**
     * 通知类消息
     */
    NOTIFICATION("notification", "通知消息"),
    
    /**
     * 验证码消息
     */
    VERIFICATION("verification", "验证码消息"),
    
    /**
     * 营销类消息
     */
    MARKETING("marketing", "营销消息"),
    
    /**
     * 警告类消息
     */
    ALERT("alert", "警告消息"),
    
    /**
     * 系统消息
     */
    SYSTEM("system", "系统消息");
    
    /**
     * 类型编码
     */
    private final String code;
    
    /**
     * 类型描述
     */
    private final String desc;
    
    MessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据编码获取消息类型
     *
     * @param code 类型编码
     * @return 消息类型枚举
     */
    public static MessageTypeEnum getByCode(String code) {
        for (MessageTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return NOTIFICATION; // 默认使用通知类型
    }
} 