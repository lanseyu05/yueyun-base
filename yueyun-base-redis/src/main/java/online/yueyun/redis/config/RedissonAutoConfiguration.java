package online.yueyun.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.ClusterServersConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Redisson自动配置类
 * 当项目中引入了redisson-spring-boot-starter依赖且没有配置RedissonClient时，自动启用此配置
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ComponentScan("online.yueyun.redis")
public class RedissonAutoConfiguration {

    /**
     * 配置RedissonClient
     * 如果项目中已经配置了RedissonClient，则不会使用此配置
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(YueyunRedisProperties properties) {
        log.info("初始化RedissonClient，模式: {}", properties.getMode());
        Config config = new Config();

        // 根据配置的模式创建对应的配置
        switch (properties.getMode().toLowerCase()) {
            case "sentinel":
                configureSentinel(config, properties);
                break;
            case "cluster":
                configureCluster(config, properties);
                break;
            case "standalone":
            default:
                configureStandalone(config, properties);
                break;
        }

        return Redisson.create(config);
    }

    /**
     * 配置单机模式
     */
    private void configureStandalone(Config config, YueyunRedisProperties properties) {
        String address = String.format("redis://%s:%d",
                properties.getHost(),
                properties.getPort());

        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(properties.getDatabase())
                .setConnectTimeout(properties.getRedisson().getConnectTimeout())
                .setRetryAttempts(properties.getRedisson().getRetryAttempts())
                .setRetryInterval(properties.getRedisson().getRetryInterval());

        if (properties.getRedisson().isUseConnectionPool()) {
            serverConfig.setConnectionPoolSize(properties.getRedisson().getConnectionPoolSize())
                    .setConnectionMinimumIdleSize(properties.getRedisson().getConnectionMinimumIdleSize());
        }

        String password = properties.getPassword();
        if (StringUtils.hasText(password)) {
            serverConfig.setPassword(password);
        }
    }

    /**
     * 配置哨兵模式
     */
    private void configureSentinel(Config config, YueyunRedisProperties properties) {
        SentinelServersConfig serverConfig = config.useSentinelServers()
                .setMasterName(properties.getSentinel().getMaster())
                .setDatabase(properties.getDatabase())
                .setConnectTimeout(properties.getRedisson().getConnectTimeout())
                .setRetryAttempts(properties.getRedisson().getRetryAttempts())
                .setRetryInterval(properties.getRedisson().getRetryInterval());

        String password = properties.getPassword();
        if (StringUtils.hasText(password)) {
            serverConfig.setPassword(password);
        }

        List<String> nodes = properties.getSentinel().getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            nodes.forEach(node -> serverConfig.addSentinelAddress("redis://" + node.trim()));
        }
    }

    /**
     * 配置集群模式
     */
    private void configureCluster(Config config, YueyunRedisProperties properties) {
        ClusterServersConfig serverConfig = config.useClusterServers()
                .setConnectTimeout(properties.getRedisson().getConnectTimeout())
                .setRetryAttempts(properties.getRedisson().getRetryAttempts())
                .setRetryInterval(properties.getRedisson().getRetryInterval());

        String password = properties.getPassword();
        if (StringUtils.hasText(password)) {
            serverConfig.setPassword(password);
        }

        List<String> nodes = properties.getCluster().getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            nodes.forEach(node -> serverConfig.addNodeAddress("redis://" + node.trim()));
        }
    }
} 