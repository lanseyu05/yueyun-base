package online.yueyun.ip.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ip.service.IpRegionService;
import online.yueyun.ip.service.IpService;
import online.yueyun.ip.service.impl.IpServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IP自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({IpProperties.class,IpRegionProperties.class})
@ConditionalOnProperty(prefix = "yueyun.ip", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IpAutoConfiguration {

    /**
     * IP服务
     *
     * @param ipRegionService IP地区服务
     * @return IP服务
     */
    @Bean
    @ConditionalOnMissingBean
    public IpService ipService(IpRegionService ipRegionService) {
        log.info("初始化IP地址服务");
        return new IpServiceImpl(ipRegionService);
    }
} 