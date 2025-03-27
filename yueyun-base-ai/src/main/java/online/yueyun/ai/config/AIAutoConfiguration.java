package online.yueyun.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.service.AIService;
import online.yueyun.ai.service.impl.DashScopeServiceImpl;
import online.yueyun.ai.util.AIFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * AI自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(AIProperties.class)
@ConditionalOnProperty(prefix = "yueyun.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AIAutoConfiguration {

    private final AIProperties properties;

    /**
     * 配置RestTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 配置AI工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public AIFactory aiFactory(AIProperties properties, RestTemplate restTemplate) {
        return new AIFactory(properties, restTemplate);
    }

    /**
     * 配置默认AI服务
     * 根据配置的默认提供商类型返回对应的服务实现
     */
    @Bean
    @ConditionalOnMissingBean
    public AIService aiService(AIFactory aiFactory, AIProperties properties) {
        String defaultProvider = properties.getDefaultProvider();
        log.info("Initializing AI service with default provider: {}", defaultProvider);
        
        if ("dashscope".equalsIgnoreCase(defaultProvider)) {
            return aiFactory.getDashScopeService();
        }
        
        // 未来可以添加其他提供商的支持
        
        throw new IllegalArgumentException("Unsupported AI provider: " + defaultProvider);
    }

    /**
     * 配置DashScope服务
     */
    @Bean
    @ConditionalOnProperty(prefix = "yueyun.ai", name = "default-provider", havingValue = "dashscope")
    @ConditionalOnMissingBean(name = "dashScopeService")
    public AIService dashScopeService(AIProperties properties, RestTemplate restTemplate) {
        log.info("Initializing DashScope service");
        
        if (properties.getDashScope() == null || properties.getDashScope().getApiKey() == null) {
            throw new IllegalArgumentException("DashScope API key is required");
        }
        
        return new DashScopeServiceImpl(properties, restTemplate);
    }


} 