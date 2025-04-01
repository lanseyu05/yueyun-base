package online.yueyun.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息配置属性
 */
@Data
@ConfigurationProperties(prefix = "yueyun.message")
public class MessageProperties {
    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 线程池配置
     */
    private ThreadPool threadPool = new ThreadPool();

    /**
     * 线程池配置类
     */
    @Data
    public static class ThreadPool {
        /**
         * 核心线程数
         */
        private int coreSize = 5;

        /**
         * 最大线程数
         */
        private int maxSize = 10;

        /**
         * 队列容量
         */
        private int queueCapacity = 100;
    }
} 