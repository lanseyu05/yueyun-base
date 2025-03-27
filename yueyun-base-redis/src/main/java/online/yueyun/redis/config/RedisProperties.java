package online.yueyun.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Redis配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.redis")
public class RedisProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 模式: standalone（单机）, sentinel（哨兵）, cluster（集群）
     */
    private String mode = "standalone";

    /**
     * 单机配置
     */
    private final Standalone standalone = new Standalone();

    /**
     * 哨兵配置
     */
    private final Sentinel sentinel = new Sentinel();

    /**
     * 集群配置
     */
    private final Cluster cluster = new Cluster();

    /**
     * 连接池配置
     */
    private final Pool pool = new Pool();

    /**
     * 缓存配置
     */
    private final Cache cache = new Cache();

    /**
     * 单机模式配置
     */
    @Data
    public static class Standalone {
        /**
         * 服务器地址
         */
        private String host = "localhost";

        /**
         * 服务器端口
         */
        private int port = 6379;

        /**
         * 数据库索引
         */
        private int database = 0;

        /**
         * 密码
         */
        private String password;
    }

    /**
     * 哨兵模式配置
     */
    @Data
    public static class Sentinel {
        /**
         * 主节点名称
         */
        private String master = "master";

        /**
         * 哨兵节点
         */
        private String nodes;

        /**
         * 密码
         */
        private String password;

        /**
         * 数据库索引
         */
        private int database = 0;
    }

    /**
     * 集群模式配置
     */
    @Data
    public static class Cluster {
        /**
         * 集群节点
         */
        private String nodes;

        /**
         * 最大重定向次数
         */
        private int maxRedirects = 3;

        /**
         * 密码
         */
        private String password;
    }

    /**
     * 连接池配置
     */
    @Data
    public static class Pool {
        /**
         * 连接池最大连接数
         */
        private int maxActive = 8;

        /**
         * 连接池最大空闲连接数
         */
        private int maxIdle = 8;

        /**
         * 连接池最小空闲连接数
         */
        private int minIdle = 0;

        /**
         * 连接池最大等待时间，单位毫秒
         */
        private long maxWait = -1;

        /**
         * 空闲连接检测间隔时间，单位毫秒
         */
        private long timeBetweenEvictionRuns = 30000;
    }

    /**
     * 缓存配置
     */
    @Data
    public static class Cache {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;

        /**
         * 全局缓存过期时间，单位：秒
         */
        private Duration ttl = Duration.ofMinutes(30);

        /**
         * 全局缓存前缀
         */
        private String keyPrefix = "yueyun:cache:";

        /**
         * 是否使用缓存空值
         */
        private boolean cacheNullValues = true;
    }
} 