package online.yueyun.storage.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.storage.enums.StorageTypeEnum;
import online.yueyun.storage.service.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 存储自动配置类
 * 
 * @author yueyun
 */
@Slf4j
@Configuration
@ComponentScan("online.yueyun.storage")
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    private final ApplicationContext applicationContext;
    private final StorageProperties storageProperties;

    public StorageAutoConfiguration(ApplicationContext applicationContext, StorageProperties storageProperties) {
        this.applicationContext = applicationContext;
        this.storageProperties = storageProperties;
    }

    /**
     * 配置默认的存储服务
     *
     * @return 默认存储服务
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "storageService")
    public StorageService storageService() {
        StorageTypeEnum storageType = StorageTypeEnum.getByCode(storageProperties.getType());
        log.info("初始化文件存储服务，使用存储类型: {}", storageType.getDesc());
        
        return switch (storageType) {
            case MINIO -> applicationContext.getBean("minioStorageService", StorageService.class);
            case ALIYUN_OSS -> applicationContext.getBean("aliyunOssStorageService", StorageService.class);
            default -> applicationContext.getBean("minioStorageService", StorageService.class);
        };
    }
} 