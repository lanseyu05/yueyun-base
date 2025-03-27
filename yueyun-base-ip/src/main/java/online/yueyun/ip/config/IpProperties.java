package online.yueyun.ip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IP配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.ip")
public class IpProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 数据库文件名称
     */
    private String dbName = "ip2region.xdb";

    /**
     * 数据库文件路径
     */
    private String dbPath = System.getProperty("user.home") + "/yueyun/ip2region.xdb";
} 