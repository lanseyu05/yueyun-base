package online.yueyun.datapermission.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.datapermission.handler.DataPermissionHandler;
import online.yueyun.datapermission.interceptor.DataPermissionInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据权限功能自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DataPermissionProperties.class)
@ConditionalOnProperty(prefix = "yueyun.datapermission", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataPermissionAutoConfiguration {

    /**
     * 配置数据权限处理器
     *
     * @param properties 配置属性
     * @return 数据权限处理器
     */
    @Bean(name = "dataPermissionHandler")
    @ConditionalOnMissingBean(name = "dataPermissionHandler")
    public DataPermissionHandler dataPermissionHandler(DataPermissionProperties properties) {
        log.info("初始化数据权限处理器");
        return new DataPermissionHandler(properties);
    }

    /**
     * 配置数据权限拦截器
     *
     * @param handler 数据权限处理器
     * @return 数据权限拦截器
     */
    @Bean(name = "dataPermissionInterceptor")
    @ConditionalOnMissingBean(name = "dataPermissionInterceptor")
    public DataPermissionInterceptor dataPermissionInterceptor(DataPermissionHandler handler) {
        log.info("初始化数据权限拦截器");
        return new DataPermissionInterceptor(handler);
    }
} 