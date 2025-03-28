package online.yueyun.ai.config;

import online.yueyun.ai.service.AiService;
import online.yueyun.ai.service.impl.DefaultAiServiceImpl;
import online.yueyun.ai.template.AiTemplateManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * AI模块自动配置类
 * 支持自定义实现扩展
 * 
 * @author yueyun
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@Import({AiTemplateManager.class})
public class AiAutoConfiguration {

    /**
     * 注册AI属性配置
     */
    @Bean
    @ConditionalOnMissingBean
    public AiProperties aiProperties() {
        return new AiProperties();
    }
    
    /**
     * 注册默认AI服务实现
     * 当没有自定义实现时生效
     */
    @Bean
    @ConditionalOnMissingBean(AiService.class)
    @ConditionalOnProperty(prefix = "yueyun.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AiService defaultAiService() {
        return new DefaultAiServiceImpl();
    }
    
    /**
     * 提供启用自定义AI服务的配置选项
     * 当配置了yueyun.ai.use-custom-service=true时生效
     */
    @Bean
    @ConditionalOnProperty(prefix = "yueyun.ai", name = "use-custom-service", havingValue = "true")
    public Object enableCustomAiService() {
        // 这是一个标记Bean，没有实际功能，仅用于条件装配
        return new Object();
    }
} 