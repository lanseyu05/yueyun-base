package online.yueyun.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息队列配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.mq")
public class MqProperties {

    /**
     * 消息队列类型：kafka, rocketmq, rabbitmq
     */
    private String type = "kafka";

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 默认主题
     */
    private String defaultTopic = "yueyun-topic";

    /**
     * 默认消费组
     */
    private String defaultGroup = "yueyun-group";

    /**
     * 消费者并发处理线程数
     */
    private Integer consumerConcurrency = 1;
    
    /**
     * 最大重试次数
     */
    private int maxRetryCount = 3;

    /**
     * Kafka配置
     */
    private final Kafka kafka = new Kafka();

    /**
     * RocketMQ配置
     */
    private final RocketMq rocketMq = new RocketMq();

    /**
     * RabbitMQ配置
     */
    private final RabbitMq rabbitMq = new RabbitMq();

    /**
     * Kafka配置
     */
    @Data
    public static class Kafka {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 服务器地址
         */
        private String bootstrapServers = "localhost:9092";

        /**
         * 生产者配置
         */
        private final Producer producer = new Producer();

        /**
         * 消费者配置
         */
        private final Consumer consumer = new Consumer();

        /**
         * 生产者配置
         */
        @Data
        public static class Producer {
            /**
             * 重试次数
             */
            private int retries = 3;

            /**
             * 批量大小
             */
            private int batchSize = 16384;

            /**
             * 缓冲区大小
             */
            private int bufferMemory = 33554432;
        }

        /**
         * 消费者配置
         */
        @Data
        public static class Consumer {
            /**
             * 自动提交
             */
            private boolean enableAutoCommit = true;

            /**
             * 自动提交间隔
             */
            private int autoCommitIntervalMs = 1000;

            /**
             * 会话超时时间
             */
            private int sessionTimeoutMs = 30000;
        }
    }

    /**
     * RocketMQ配置
     */
    @Data
    public static class RocketMq {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 服务器地址
         */
        private String nameServer = "localhost:9876";

        /**
         * 生产者组
         */
        private String producerGroup = "yueyun-producer-group";

        /**
         * 消费者组
         */
        private String consumerGroup = "yueyun-consumer-group";

        /**
         * 发送消息超时时间
         */
        private int sendMessageTimeout = 3000;

        /**
         * 压缩消息体阈值
         */
        private int compressMsgBodyOverHowmuch = 4096;
        
        /**
         * 最大重试次数
         */
        private int maxRetryCount = 3;
    }

    /**
     * RabbitMQ配置
     */
    @Data
    public static class RabbitMq {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 服务器地址
         */
        private String host = "localhost";

        /**
         * 端口
         */
        private int port = 5672;

        /**
         * 用户名
         */
        private String username = "guest";

        /**
         * 密码
         */
        private String password = "guest";

        /**
         * 虚拟主机
         */
        private String virtualHost = "/";
    }
} 