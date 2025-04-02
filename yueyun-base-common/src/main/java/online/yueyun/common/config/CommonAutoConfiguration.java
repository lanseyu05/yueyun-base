package online.yueyun.common.config;

import lombok.RequiredArgsConstructor;
import online.yueyun.common.interceptor.TokenInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
@RequiredArgsConstructor
public class CommonAutoConfiguration implements WebMvcConfigurer {
    /**
     * Token拦截器
     */
    private final TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error");
    }
} 