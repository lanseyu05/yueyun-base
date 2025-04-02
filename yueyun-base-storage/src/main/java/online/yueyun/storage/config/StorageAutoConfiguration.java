package online.yueyun.storage.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.storage.service.StorageService;
import online.yueyun.storage.service.impl.MinioStorageServiceImpl;
import online.yueyun.storage.service.impl.StorageServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储功能自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(prefix = "yueyun.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StorageAutoConfiguration {

    /**
     * 配置存储服务
     *
     * @param properties 配置属性
     * @return 存储服务
     */
    @Bean(name = "storageService")
    @ConditionalOnMissingBean(name = "storageService")
    public StorageService storageService(StorageProperties properties) {
        log.info("初始化存储服务");
        return new MinioStorageServiceImpl(properties);
    }
} 