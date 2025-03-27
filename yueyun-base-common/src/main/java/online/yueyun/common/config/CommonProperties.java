package online.yueyun.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通用配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.common")
public class CommonProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 应用名称
     */
    private String applicationName;
    
    /**
     * 是否开启接口文档
     */
    private boolean enableSwagger = true;
} 