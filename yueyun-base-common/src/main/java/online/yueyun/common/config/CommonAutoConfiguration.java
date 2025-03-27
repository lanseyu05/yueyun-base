package online.yueyun.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 通用功能自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(CommonProperties.class)
@ConditionalOnProperty(prefix = "yueyun.common", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan("online.yueyun.common")
public class CommonAutoConfiguration {

    /**
     * 配置Properties
     */
    private final CommonProperties properties;

    /**
     * 构造方法
     *
     * @param properties 配置属性
     */
    public CommonAutoConfiguration(CommonProperties properties) {
        this.properties = properties;
    }

    /**
     * 全局异常处理器
     *
     * @return GlobalExceptionHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
} 