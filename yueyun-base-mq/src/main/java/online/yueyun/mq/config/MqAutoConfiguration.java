package online.yueyun.mq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import online.yueyun.mq.kafka.KafkaConfiguration;
import online.yueyun.mq.rabbitmq.RabbitMqConfiguration;
import online.yueyun.mq.rocketmq.RocketMqConfiguration;

/**
 * 消息队列自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MqProperties.class)
@ConditionalOnProperty(prefix = "yueyun.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({KafkaConfiguration.class})
public class MqAutoConfiguration {

    /**
     * 根据配置的消息队列类型导入相应的配置类
     *
     * @return 消息队列配置选择器
     */
    @Bean
    @ConditionalOnMissingBean
    public MqConfigurationSelector mqConfigurationSelector(MqProperties mqProperties) {
        return new MqConfigurationSelector(mqProperties);
    }

    /**
     * 消息队列配置选择器，用于根据配置动态选择消息队列实现
     */
    public static class MqConfigurationSelector {

        public MqConfigurationSelector(MqProperties properties) {
            // 这里不再尝试动态修改@Import注解，因为无法在运行时修改注解
            // 通过条件Bean的方式在各自的配置类中控制是否启用
            String type = properties.getType().toLowerCase();
            switch (type) {
                case "kafka":
                    if (!properties.getKafka().isEnabled()) {
                        throw new IllegalStateException("Kafka已配置但未启用");
                    }
                    break;
                case "rocketmq":
                    if (!properties.getRocketMq().isEnabled()) {
                        throw new IllegalStateException("RocketMQ已配置但未启用");
                    }
                    break;
                case "rabbitmq":
                    if (!properties.getRabbitMq().isEnabled()) {
                        throw new IllegalStateException("RabbitMQ已配置但未启用");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("不支持的消息队列类型: " + type);
            }
        }
    }
}