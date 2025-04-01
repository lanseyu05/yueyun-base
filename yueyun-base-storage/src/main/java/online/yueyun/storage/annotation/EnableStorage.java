package online.yueyun.storage.annotation;

import online.yueyun.storage.config.StorageAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用存储功能注解
 * 在SpringBoot启动类上添加此注解以启用存储功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(StorageAutoConfiguration.class)
public @interface EnableStorage {
} 