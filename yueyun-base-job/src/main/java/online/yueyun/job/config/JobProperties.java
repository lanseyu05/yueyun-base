package online.yueyun.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定时作业配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.job")
public class JobProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 执行器配置
     */
    private final Executor executor = new Executor();

    /**
     * 调度中心配置
     */
    private final Admin admin = new Admin();

    /**
     * 执行器配置
     */
    @Data
    public static class Executor {
        /**
         * 执行器AppName
         */
        private String appName = "yueyun-job-executor";

        /**
         * 执行器注册地址，优先使用该配置作为注册地址
         */
        private String address;

        /**
         * 执行器IP，默认为空表示自动获取IP
         */
        private String ip;

        /**
         * 执行器端口号，小于等于0则随机
         */
        private int port = 9999;

        /**
         * 执行器日志路径
         */
        private String logPath = "./logs/yueyun-job/jobhandler";

        /**
         * 执行器日志保留天数，-1表示永久保存
         */
        private int logRetentionDays = 30;
    }

    /**
     * 调度中心配置
     */
    @Data
    public static class Admin {
        /**
         * 调度中心地址，支持集群部署，多个地址用逗号分隔
         */
        private String addresses = "http://localhost:8080/xxl-job-admin";

        /**
         * 调度中心登录账号
         */
        private String username = "admin";

        /**
         * 调度中心登录密码
         */
        private String password = "123456";

        /**
         * 调度中心通讯TOKEN
         */
        private String accessToken = "";
    }
} 