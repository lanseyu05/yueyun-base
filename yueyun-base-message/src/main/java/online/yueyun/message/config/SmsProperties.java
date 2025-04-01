package online.yueyun.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信配置属性
 */
@Data
@ConfigurationProperties(prefix = "yueyun.message.sms")
public class SmsProperties {
    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 短信服务商（aliyun, tencent, qiniu等）
     */
    private String provider = "aliyun";

    /**
     * AccessKey
     */
    private String accessKey;

    /**
     * SecretKey
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 默认模板编码
     */
    private String defaultTemplateCode;
} 