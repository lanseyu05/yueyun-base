package online.yueyun.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis自动配置类
 * 当项目中引入了spring-boot-starter-data-redis依赖且没有配置RedisTemplate时，自动启用此配置
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ComponentScan("online.yueyun.redis")
public class RedisAutoConfiguration {

    /**
     * 配置RedisTemplate
     * 如果项目中已经配置了RedisTemplate，则不会使用此配置
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value值
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置Redis连接工厂
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(YueyunRedisProperties properties) {
        // 创建Redis连接配置
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(properties.getHost());
        redisConfig.setPort(properties.getPort());
        redisConfig.setDatabase(properties.getDatabase());
        
        if (properties.getPassword() != null) {
            redisConfig.setPassword(properties.getPassword());
        }

        // 创建Lettuce连接池配置
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(properties.getRedisson().getConnectTimeout()))
                .build();

        // 创建Lettuce连接工厂
        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }
} 