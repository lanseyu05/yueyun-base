package online.yueyun.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信配置属性
 */
@Data
@ConfigurationProperties(prefix = "yueyun.message.wechat")
public class WechatProperties {
    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 公众号AppID
     */
    private String appId;

    /**
     * 公众号AppSecret
     */
    private String appSecret;

    /**
     * 公众号Token
     */
    private String token;

    /**
     * 公众号AES Key
     */
    private String aesKey;

    /**
     * 模板消息配置
     */
    private Template template = new Template();

    /**
     * 模板消息配置类
     */
    @Data
    public static class Template {
        /**
         * 默认模板ID
         */
        private String defaultTemplateId;
    }
} 