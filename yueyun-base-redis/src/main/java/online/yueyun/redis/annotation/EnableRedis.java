package online.yueyun.redis.annotation;

import online.yueyun.redis.config.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用Redis功能注解
 * 在SpringBoot启动类上添加此注解以启用Redis功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RedisAutoConfiguration.class)
public @interface EnableRedis {
} 