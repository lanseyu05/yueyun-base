package online.yueyun.datapermission.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.datapermission.handler.DataPermissionHandler;
import online.yueyun.datapermission.interceptor.DataPermissionInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 数据权限自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DataPermissionProperties.class)
@ConditionalOnClass(MybatisPlusInterceptor.class)
@ConditionalOnProperty(prefix = "yueyun.datapermission", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataPermissionAutoConfiguration {

    /**
     * 数据权限处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public DataPermissionHandler dataPermissionHandler(DataPermissionProperties properties) {
        // 默认实现，返回一个简单的处理器
        return new DataPermissionHandler() {
            @Override
            public net.sf.jsqlparser.expression.Expression getSqlSegment(net.sf.jsqlparser.expression.Expression where, online.yueyun.datapermission.annotation.DataPermission dataPermission) {
                // 这里应该根据当前用户的角色和所配置的数据权限生成对应的SQL条件
                // 默认实现仅返回原WHERE条件
                log.info("数据权限类型: {}, 资源标识: {}", dataPermission.type(), dataPermission.resource());
                return where;
            }
        };
    }

    /**
     * 添加数据权限拦截器到MybatisPlusInterceptor
     */
    @Bean
    @ConditionalOnBean({MybatisPlusInterceptor.class, DataPermissionHandler.class})
    public InnerInterceptor dataPermissionInterceptor(DataPermissionHandler dataPermissionHandler, MybatisPlusInterceptor mybatisPlusInterceptor) {
        DataPermissionInterceptor interceptor = new DataPermissionInterceptor(dataPermissionHandler);
        List<InnerInterceptor> interceptors = mybatisPlusInterceptor.getInterceptors();
        interceptors.add(interceptor);
        log.info("数据权限拦截器注册成功");
        return interceptor;
    }
} 