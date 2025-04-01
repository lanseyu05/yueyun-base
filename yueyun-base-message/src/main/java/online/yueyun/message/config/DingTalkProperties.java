package online.yueyun.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 钉钉配置属性
 */
@Data
@ConfigurationProperties(prefix = "yueyun.message.dingtalk")
public class DingTalkProperties {
    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 应用AppKey
     */
    private String appKey;

    /**
     * 应用AppSecret
     */
    private String appSecret;

    /**
     * 企业ID
     */
    private String corpId;

    /**
     * 企业内部应用AgentId
     */
    private String agentId;

    /**
     * 钉钉群机器人Webhook
     */
    private Robot robot = new Robot();

    /**
     * 钉钉机器人配置
     */
    @Data
    public static class Robot {
        /**
         * 钉钉机器人Webhook地址
         */
        private String webhookUrl;

        /**
         * 钉钉机器人安全设置Secret
         */
        private String secret;
    }
} 