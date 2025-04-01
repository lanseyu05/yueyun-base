package online.yueyun.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮件配置属性
 */
@Data
@ConfigurationProperties(prefix = "spring.mail")
public class EmailProperties {
    /**
     * 邮件服务器地址
     */
    private String host;

    /**
     * 邮件服务器端口
     */
    private Integer port;

    /**
     * 邮件用户名
     */
    private String username;

    /**
     * 邮件密码
     */
    private String password;

    /**
     * 默认编码
     */
    private String defaultEncoding = "UTF-8";

    /**
     * 是否启用TLS
     */
    private Boolean starttlsEnable = true;

    /**
     * 是否启用SSL
     */
    private Boolean sslEnable = true;
} 