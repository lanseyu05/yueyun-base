package online.yueyun.redis.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.redis.service.RedisService;
import online.yueyun.redis.service.impl.RedisServiceImpl;
import online.yueyun.redis.template.RedisTemplateWrapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty(prefix = "yueyun.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisAutoConfiguration {

    /**
     * 配置LettuceConnectionFactory
     *
     * @param properties Redis配置属性
     * @return LettuceConnectionFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public LettuceConnectionFactory lettuceConnectionFactory(RedisProperties properties) {
        log.info("初始化Redis连接工厂，模式: {}", properties.getMode());
        
        // 创建Lettuce连接池配置
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(properties.getPool().getMaxIdle());
        poolConfig.setMinIdle(properties.getPool().getMinIdle());
        poolConfig.setMaxTotal(properties.getPool().getMaxActive());
        poolConfig.setMaxWaitMillis(properties.getPool().getMaxWait());
        poolConfig.setTimeBetweenEvictionRunsMillis(properties.getPool().getTimeBetweenEvictionRuns());
        
        // 创建Lettuce客户端配置
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .build();
        
        // 根据配置的模式创建对应的连接工厂
        RedisConnectionFactory connectionFactory;
        
        switch (properties.getMode().toLowerCase()) {
            case "sentinel":
                connectionFactory = createSentinelConnectionFactory(properties, clientConfig);
                break;
            case "cluster":
                connectionFactory = createClusterConnectionFactory(properties, clientConfig);
                break;
            case "standalone":
            default:
                connectionFactory = createStandaloneConnectionFactory(properties, clientConfig);
                break;
        }
        
        return (LettuceConnectionFactory) connectionFactory;
    }
    
    /**
     * 创建单机模式连接工厂
     */
    private RedisConnectionFactory createStandaloneConnectionFactory(RedisProperties properties, 
                                                                    LettuceClientConfiguration clientConfig) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(properties.getStandalone().getHost());
        config.setPort(properties.getStandalone().getPort());
        config.setDatabase(properties.getStandalone().getDatabase());
        
        String password = properties.getStandalone().getPassword();
        if (StringUtils.hasText(password)) {
            config.setPassword(RedisPassword.of(password));
        }
        
        return new LettuceConnectionFactory(config, clientConfig);
    }
    
    /**
     * 创建哨兵模式连接工厂
     */
    private RedisConnectionFactory createSentinelConnectionFactory(RedisProperties properties, 
                                                                 LettuceClientConfiguration clientConfig) {
        RedisSentinelConfiguration config = new RedisSentinelConfiguration();
        config.setMaster(properties.getSentinel().getMaster());
        config.setDatabase(properties.getSentinel().getDatabase());
        
        String password = properties.getSentinel().getPassword();
        if (StringUtils.hasText(password)) {
            config.setPassword(RedisPassword.of(password));
        }
        
        String nodes = properties.getSentinel().getNodes();
        if (StringUtils.hasText(nodes)) {
            Set<RedisNode> sentinels = new HashSet<>();
            for (String node : nodes.split(",")) {
                String[] parts = node.trim().split(":");
                if (parts.length == 2) {
                    sentinels.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
                }
            }
            config.setSentinels(sentinels);
        }
        
        return new LettuceConnectionFactory(config, clientConfig);
    }
    
    /**
     * 创建集群模式连接工厂
     */
    private RedisConnectionFactory createClusterConnectionFactory(RedisProperties properties, 
                                                                LettuceClientConfiguration clientConfig) {
        RedisClusterConfiguration config = new RedisClusterConfiguration();
        
        String password = properties.getCluster().getPassword();
        if (StringUtils.hasText(password)) {
            config.setPassword(RedisPassword.of(password));
        }
        
        config.setMaxRedirects(properties.getCluster().getMaxRedirects());
        
        String nodes = properties.getCluster().getNodes();
        if (StringUtils.hasText(nodes)) {
            Set<RedisNode> clusterNodes = new HashSet<>();
            for (String node : nodes.split(",")) {
                String[] parts = node.trim().split(":");
                if (parts.length == 2) {
                    clusterNodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
                }
            }
            config.setClusterNodes(clusterNodes);
        }
        
        return new LettuceConnectionFactory(config, clientConfig);
    }
    
    /**
     * 配置RedisTemplate
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("初始化RedisTemplate");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        
        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        
        // 开启事务支持
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        
        return template;
    }
    
    /**
     * 配置缓存管理器
     *
     * @param connectionFactory Redis连接工厂
     * @param properties Redis配置属性
     * @return CacheManager
     */
    @Bean
    @ConditionalOnProperty(prefix = "yueyun.redis.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, RedisProperties properties) {
        log.info("初始化RedisCacheManager");
        
        // 配置序列化
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存前缀
                .prefixCacheNameWith(properties.getCache().getKeyPrefix())
                // 设置key和value的序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                // 设置过期时间
                .entryTtl(properties.getCache().getTtl());
        
        // 设置是否缓存空值
        if (!properties.getCache().isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
    
    /**
     * 配置RedisTemplateWrapper
     *
     * @param redisTemplate RedisTemplate
     * @return RedisTemplateWrapper
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplateWrapper redisTemplateWrapper(RedisTemplate<String, Object> redisTemplate) {
        log.info("初始化RedisTemplateWrapper");
        return new RedisTemplateWrapper(redisTemplate);
    }
    
    /**
     * 配置RedisService
     *
     * @param redisTemplateWrapper RedisTemplateWrapper
     * @return RedisService
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisService redisService(RedisTemplateWrapper redisTemplateWrapper) {
        log.info("初始化RedisService");
        return new RedisServiceImpl(redisTemplateWrapper);
    }
} 