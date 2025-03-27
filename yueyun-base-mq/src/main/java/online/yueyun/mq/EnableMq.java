package online.yueyun.mq;

import org.springframework.context.annotation.Import;
import online.yueyun.mq.config.MqAutoConfiguration;

import java.lang.annotation.*;

/**
 * 启用消息队列功能的注解
 * <p>
 * 在Spring Boot应用中使用此注解来启用消息队列功能，
 * 可通过application.yml或application.properties配置消息队列属性
 * </p>
 * 
 * 配置示例：
 * <pre>
 * yueyun:
 *   mq:
 *     type: kafka  # 可选：kafka, rocketmq, rabbitmq
 *     enabled: true
 *     default-topic: yueyun-topic
 *     default-group: yueyun-group
 *     kafka:
 *       bootstrap-servers: localhost:9092
 * </pre>
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MqAutoConfiguration.class)
public @interface EnableMq {
} 