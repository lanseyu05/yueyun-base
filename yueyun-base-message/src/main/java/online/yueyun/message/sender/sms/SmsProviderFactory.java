package online.yueyun.message.sender.sms;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.SmsProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信服务提供商工厂
 * 负责管理和提供不同的短信服务实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsProviderFactory {
    private final List<SmsProvider> smsProviders;
    private final SmsProperties smsProperties;
    private final Map<String, SmsProvider> providerMap = new HashMap<>();
    
    /**
     * 初始化方法，加载所有短信服务提供商
     */
    @PostConstruct
    public void init() {
        smsProviders.forEach(provider -> {
            String name = provider.getName();
            providerMap.put(name.toLowerCase(), provider);
            log.info("注册短信服务提供商: {}", name);
        });
    }
    
    /**
     * 获取默认的短信服务提供商
     * 
     * @return 默认的短信服务提供商
     */
    public SmsProvider getDefaultProvider() {
        String providerName = smsProperties.getProvider().toLowerCase();
        return getProvider(providerName);
    }
    
    /**
     * 根据名称获取短信服务提供商
     * 
     * @param name 提供商名称
     * @return 短信服务提供商
     */
    public SmsProvider getProvider(String name) {
        SmsProvider provider = providerMap.get(name.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("不支持的短信服务提供商: " + name);
        }
        return provider;
    }
    
    /**
     * 判断是否支持指定的短信服务提供商
     * 
     * @param name 提供商名称
     * @return 是否支持
     */
    public boolean isSupported(String name) {
        return providerMap.containsKey(name.toLowerCase());
    }
} 