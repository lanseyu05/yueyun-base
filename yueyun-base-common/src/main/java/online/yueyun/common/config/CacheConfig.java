package online.yueyun.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 提供默认的Caffeine缓存配置
 *
 * @author YueYun
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 默认缓存配置
     */
    private static final int DEFAULT_MAX_SIZE = 10000;
    private static final int DEFAULT_EXPIRE_HOURS = 24;

    /**
     * 配置缓存管理器
     * 如果项目中已经配置了CacheManager，则不会使用此配置
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(DEFAULT_EXPIRE_HOURS, TimeUnit.HOURS)  // 缓存24小时后过期
                .maximumSize(DEFAULT_MAX_SIZE)                           // 最大缓存10000条记录
                .recordStats());                                        // 开启统计
        return cacheManager;
    }

} 