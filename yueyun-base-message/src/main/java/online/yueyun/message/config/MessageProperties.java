package online.yueyun.message.config;

import lombok.Data;
import online.yueyun.message.enums.MessageChannelEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 消息服务配置属性
 * 
 * @author yueyun
 */
@Data
@ConfigurationProperties(prefix = "yueyun.message")
public class MessageProperties {

    /**
     * 默认的消息渠道，可使用逗号分隔配置多个，默认为邮件
     */
    private String defaultChannels = MessageChannelEnum.EMAIL.getCode();
    
    /**
     * 是否开启异步发送
     */
    private boolean asyncEnabled = true;
    
    /**
     * 异步线程池核心线程数
     */
    private int asyncCorePoolSize = 5;
    
    /**
     * 异步线程池最大线程数
     */
    private int asyncMaxPoolSize = 10;
    
    /**
     * 异步线程池队列容量
     */
    private int asyncQueueCapacity = 100;
    
    /**
     * 线程池名称前缀
     */
    private String asyncThreadNamePrefix = "message-async-";
    
    /**
     * 邮件配置
     */
    private final EmailConfig email = new EmailConfig();
    
    /**
     * 短信配置
     */
    private final SmsConfig sms = new SmsConfig();
    
    /**
     * 飞书配置
     */
    private final FeishuConfig feishu = new FeishuConfig();
    
    /**
     * 钉钉配置
     */
    private final DingtalkConfig dingtalk = new DingtalkConfig();
    
    /**
     * 邮件配置
     */
    @Data
    public static class EmailConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 邮件服务器地址
         */
        private String host;
        
        /**
         * 邮件服务器端口
         */
        private Integer port;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 密码
         */
        private String password;
        
        /**
         * 默认发件人
         */
        private String defaultFrom;
        
        /**
         * 默认编码
         */
        private String defaultEncoding = "UTF-8";
        
        /**
         * 是否启用SSL
         */
        private boolean sslEnabled = true;
    }
    
    /**
     * 短信配置
     */
    @Data
    public static class SmsConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 短信服务提供商（aliyun/tencent）
         */
        private String provider = "aliyun";
        
        /**
         * 阿里云配置
         */
        private final AliyunSmsConfig aliyun = new AliyunSmsConfig();
        
        /**
         * 腾讯云配置
         */
        private final TencentSmsConfig tencent = new TencentSmsConfig();
        
        /**
         * 阿里云短信配置
         */
        @Data
        public static class AliyunSmsConfig {
            /**
             * 访问密钥ID
             */
            private String accessKeyId;
            
            /**
             * 访问密钥密文
             */
            private String accessKeySecret;
            
            /**
             * 短信签名
             */
            private String signName;
            
            /**
             * 地域ID
             */
            private String regionId = "cn-hangzhou";
        }
        
        /**
         * 腾讯云短信配置
         */
        @Data
        public static class TencentSmsConfig {
            /**
             * 访问密钥ID
             */
            private String secretId;
            
            /**
             * 访问密钥密文
             */
            private String secretKey;
            
            /**
             * 应用ID
             */
            private String appId;
            
            /**
             * 短信签名
             */
            private String signName;
            
            /**
             * 地域
             */
            private String region = "ap-guangzhou";
        }
    }
    
    /**
     * 飞书配置
     */
    @Data
    public static class FeishuConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 应用ID
         */
        private String appId;
        
        /**
         * 应用密钥
         */
        private String appSecret;
        
        /**
         * 是否启用Webhook
         */
        private boolean webhookEnabled = false;
        
        /**
         * Webhook地址列表
         */
        private List<String> webhookUrls;
    }
    
    /**
     * 钉钉配置
     */
    @Data
    public static class DingtalkConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 应用Key
         */
        private String appKey;
        
        /**
         * 应用密钥
         */
        private String appSecret;
        
        /**
         * 是否启用Webhook
         */
        private boolean webhookEnabled = false;
        
        /**
         * Webhook地址列表
         */
        private List<String> webhookUrls;
        
        /**
         * 签名密钥（用于加签模式）
         */
        private String signSecret;
    }
} 