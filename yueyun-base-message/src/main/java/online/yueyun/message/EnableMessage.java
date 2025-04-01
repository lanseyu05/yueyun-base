package online.yueyun.message;

import online.yueyun.message.config.MessageAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用消息功能注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MessageAutoConfiguration.class)
public @interface EnableMessage {
} 