package online.yueyun.skywalking.annotation;

import online.yueyun.skywalking.config.TracingAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用链路追踪注解
 * 用于启用链路追踪功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TracingAutoConfiguration.class)
public @interface EnableTracing {
} 