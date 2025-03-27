package online.yueyun.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XXL-JOB配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.xxl.job")
public class XxlJobProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 调度中心配置
     */
    private final Admin admin = new Admin();

    /**
     * 执行器配置
     */
    private final Executor executor = new Executor();

    /**
     * 调度中心配置
     */
    @Data
    public static class Admin {
        /**
         * 调度中心地址，如http://xxl-job-admin.example.com/xxl-job-admin
         */
        private String addresses;
    }

    /**
     * 执行器配置
     */
    @Data
    public static class Executor {
        /**
         * 执行器AppName
         */
        private String appName = "xxl-job-executor";

        /**
         * 执行器注册方式: AUTO(0), IP(1), HOSTNAME(2)
         */
        private String addressType = "AUTO";

        /**
         * 执行器IP
         */
        private String ip;

        /**
         * 执行器端口
         */
        private int port = 9999;

        /**
         * 执行器日志路径
         */
        private String logPath = "./logs/xxl-job";

        /**
         * 执行器日志保留天数
         */
        private int logRetentionDays = 30;

        /**
         * 访问令牌
         */
        private String accessToken;
    }
} 