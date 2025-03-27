package online.yueyun.skywalking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.skywalking.aspect.TracingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 链路追踪自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TracingProperties.class)
@ConditionalOnProperty(prefix = "yueyun.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TracingAutoConfiguration {

    /**
     * 链路追踪切面
     *
     * @param properties 配置属性
     * @param objectMapper JSON转换器
     * @return 链路追踪切面
     */
    @Bean
    @ConditionalOnMissingBean
    public TracingAspect tracingAspect(TracingProperties properties, ObjectMapper objectMapper) {
        log.info("初始化链路追踪切面");
        return new TracingAspect(properties, objectMapper);
    }

    /**
     * JSON转换器
     *
     * @return JSON转换器
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
} 