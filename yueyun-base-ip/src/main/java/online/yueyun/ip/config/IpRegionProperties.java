package online.yueyun.ip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IP地址检索配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.ip")
public class IpRegionProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 数据库文件路径
     * 如果为空，将使用内置数据库
     */
    private String dbPath = "";

    /**
     * 是否自动加载数据库
     */
    private boolean autoLoad = true;

    /**
     * 数据库缓存类型：file, memory
     * file: 从本地加载，以减少内存使用
     * memory: 加载到内存，查询更快
     */
    private String cacheType = "memory";

    /**
     * 是否启用缓存
     */
    private boolean enableCache = true;

    /**
     * 缓存大小
     */
    private int cacheSize = 5000;

    /**
     * 缓存过期时间（秒）
     */
    private int cacheExpire = 3600;
} 