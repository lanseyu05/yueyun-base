package online.yueyun.job.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * XXL-JOB自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnProperty(prefix = "yueyun.xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobAutoConfiguration {

    /**
     * 配置XXL-JOB执行器
     *
     * @param properties 配置属性
     * @param environment 环境配置
     * @return XXL-JOB执行器
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties properties, Environment environment) {
        log.info("初始化XXL-JOB执行器");
        
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        
        // 设置调度中心地址
        xxlJobSpringExecutor.setAdminAddresses(properties.getAdmin().getAddresses());
        
        // 设置执行器AppName
        String appName = properties.getExecutor().getAppName();
        if (!StringUtils.hasText(appName)) {
            // 如果AppName为空，则使用Spring应用名称
            appName = environment.getProperty("spring.application.name", "xxl-job-executor");
        }
        xxlJobSpringExecutor.setAppname(appName);
        
        // 设置执行器IP
        xxlJobSpringExecutor.setIp(properties.getExecutor().getIp());
        
        // 设置执行器端口
        xxlJobSpringExecutor.setPort(properties.getExecutor().getPort());
        
        // 设置执行器访问令牌
        xxlJobSpringExecutor.setAccessToken(properties.getExecutor().getAccessToken());
        
        // 设置执行器日志路径
        xxlJobSpringExecutor.setLogPath(properties.getExecutor().getLogPath());
        
        // 设置执行器日志保留天数
        xxlJobSpringExecutor.setLogRetentionDays(properties.getExecutor().getLogRetentionDays());
        
        return xxlJobSpringExecutor;
    }
} 