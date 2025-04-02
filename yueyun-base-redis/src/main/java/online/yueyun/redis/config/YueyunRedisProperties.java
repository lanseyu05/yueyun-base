package online.yueyun.redis.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 扩展Spring Boot的Redis配置属性
 * 继承自RedisProperties，添加Redisson相关配置
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class YueyunRedisProperties extends RedisProperties {

    /**
     * Redis连接模式：standalone（单机）、sentinel（哨兵）、cluster（集群）
     */
    private String mode = "standalone";

    /**
     * Redisson配置
     */
    private Redisson redisson = new Redisson();

    /**
     * Redisson配置类
     */
    @Data
    public static class Redisson {
        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 10000;

        /**
         * 重试次数
         */
        private int retryAttempts = 3;

        /**
         * 重试间隔（毫秒）
         */
        private int retryInterval = 1500;

        /**
         * 是否启用SSL
         */
        private boolean ssl = false;

        /**
         * 是否使用连接池
         */
        private boolean useConnectionPool = true;

        /**
         * 连接池大小
         */
        private int connectionPoolSize = 64;

        /**
         * 连接池最小空闲连接数
         */
        private int connectionMinimumIdleSize = 24;
    }
} 