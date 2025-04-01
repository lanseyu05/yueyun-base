package online.yueyun.ai.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.service.AiService;
import online.yueyun.ai.service.impl.AiServiceImpl;
import online.yueyun.ai.template.AiTemplateManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * AI功能自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@Import({AiTemplateManager.class})
@ConditionalOnProperty(prefix = "yueyun.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiAutoConfiguration {

    /**
     * 配置AI服务
     *
     * @param properties 配置属性
     * @param templateManager 模板管理器
     * @return AI服务
     */
    @Bean(name = "aiService")
    @ConditionalOnMissingBean(name = "aiService")
    public AiService aiService(AiProperties properties, AiTemplateManager templateManager) {
        log.info("初始化AI服务");
        return new AiServiceImpl(properties, templateManager);
    }
    
    /**
     * 注册AI属性配置
     */
    @Bean
    @ConditionalOnMissingBean
    public AiProperties aiProperties() {
        return new AiProperties();
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