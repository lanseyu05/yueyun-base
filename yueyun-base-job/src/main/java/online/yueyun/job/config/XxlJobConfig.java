package online.yueyun.job.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-JOB配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobConfig {
    
    /**
     * 调度中心地址
     */
    private String adminAddresses;
    
    /**
     * 执行器名称
     */
    private String executorName;
    
    /**
     * 执行器端口
     */
    private int executorPort;
    
    /**
     * 执行器日志路径
     */
    private String executorLogPath;
    
    /**
     * 执行器日志保留天数
     */
    private int executorLogRetentionDays;
    
    /**
     * 执行器AppName
     */
    private String executorAppname;
    
    /**
     * 执行器注册地址
     */
    private String executorAddress;
    
    /**
     * 执行器IP
     */
    private String executorIp;
    
    /**
     * 访问令牌
     */
    private String accessToken;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(executorAppname);
        xxlJobSpringExecutor.setAddress(executorAddress);
        xxlJobSpringExecutor.setIp(executorIp);
        xxlJobSpringExecutor.setPort(executorPort);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(executorLogPath);
        xxlJobSpringExecutor.setLogRetentionDays(executorLogRetentionDays);
        return xxlJobSpringExecutor;
    }
} 