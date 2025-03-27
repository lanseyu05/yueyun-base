package online.yueyun.ai.annotation;

import online.yueyun.ai.config.AIAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用AI功能注解
 * 在SpringBoot启动类上添加此注解以启用AI功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AIAutoConfiguration.class)
public @interface EnableAI {
} 