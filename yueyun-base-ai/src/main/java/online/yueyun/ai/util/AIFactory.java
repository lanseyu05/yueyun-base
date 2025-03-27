package online.yueyun.ai.util;

import online.yueyun.ai.config.AIProperties;
import online.yueyun.ai.service.AIService;
import online.yueyun.ai.service.impl.DashScopeServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI服务工厂类
 * 负责创建和管理不同AI服务实例
 *
 * @author yueyun
 */
public class AIFactory {

    private static final Logger logger = LoggerFactory.getLogger(AIFactory.class);

    private final AIProperties properties;
    private final RestTemplate restTemplate;

    // 缓存已创建的服务实例
    private final Map<String, AIService> serviceCache = new ConcurrentHashMap<>();

    public AIFactory(AIProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * 获取DashScope服务实例
     *
     * @return DashScope服务实例
     */
    public AIService getDashScopeService() {
        return getService("dashscope");
    }

    /**
     * 根据提供商名称获取AI服务实例
     * 如果服务已存在则从缓存返回，否则创建新实例
     *
     * @param provider 提供商名称
     * @return AI服务实例
     */
    public AIService getService(String provider) {
        return serviceCache.computeIfAbsent(provider, this::createService);
    }

    /**
     * 创建指定提供商的AI服务实例
     *
     * @param provider 提供商名称
     * @return AI服务实例
     */
    private AIService createService(String provider) {
        logger.info("创建AI服务: {}", provider);

        if ("dashscope".equalsIgnoreCase(provider)) {
            validateDashScopeConfig();
            return new DashScopeServiceImpl(properties, restTemplate);
        }

        // 未来可以添加其他供应商的支持

        throw new IllegalArgumentException("不支持的AI提供商: " + provider);
    }

    /**
     * 验证DashScope配置
     */
    private void validateDashScopeConfig() {
        if (properties.getDashScope() == null || properties.getDashScope().getApiKey() == null) {
            throw new IllegalArgumentException("DashScope API密钥未配置");
        }
    }
} 