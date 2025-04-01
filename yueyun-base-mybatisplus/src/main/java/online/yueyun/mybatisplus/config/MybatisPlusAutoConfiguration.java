package online.yueyun.mybatisplus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mybatisplus.handler.MybatisPlusFillHandler;
import online.yueyun.mybatisplus.tenant.CustomTenantLineHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MybatisPlusProperties.class)
@ConditionalOnProperty(prefix = "yueyun.mybatis-plus", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MybatisPlusAutoConfiguration {

    private final MybatisPlusProperties properties;

    /**
     * 配置MybatisPlus插件
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("初始化MybatisPlusInterceptor");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        if (properties.getPagination().isEnabled()) {
            PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
            
            // 设置数据库类型
            String dbType = properties.getPagination().getDbType();
            if (dbType != null && !dbType.isEmpty()) {
                try {
                    paginationInterceptor.setDbType(DbType.getDbType(dbType));
                } catch (Exception e) {
                    log.warn("无效的数据库类型 [{}]，使用默认值", dbType);
                    paginationInterceptor.setDbType(DbType.MYSQL);
                }
            }
            
            // 设置最大单页限制数量
            paginationInterceptor.setMaxLimit(properties.getPagination().getMaxLimit());
            
            // 设置分页插件优化
            paginationInterceptor.setOptimizeJoin(properties.getPagination().isOptimizeJoin());
            
            interceptor.addInnerInterceptor(paginationInterceptor);
            log.info("分页插件已启用");
        }

        // 添加乐观锁插件
        if (properties.isOptimisticLock()) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
            log.info("乐观锁插件已启用");
        }

        // 添加防止全表更新与删除插件
        if (properties.isBlockAttack()) {
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
            log.info("防止全表更新与删除插件已启用");
        }

        // 添加多租户插件
        if (properties.getTenant().isEnabled()) {
            TenantLineHandler tenantLineHandler = new CustomTenantLineHandler();
            TenantLineInnerInterceptor tenantLineInnerInterceptor = new TenantLineInnerInterceptor(tenantLineHandler);
            interceptor.addInnerInterceptor(tenantLineInnerInterceptor);
            log.info("多租户插件已启用");
        }

        return interceptor;
    }

    /**
     * 配置字段自动填充处理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "yueyun.mybatis-plus.field-fill", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MybatisPlusFillHandler mybatisPlusFillHandler() {
        return new MybatisPlusFillHandler(properties);
    }
} 