package online.yueyun.datapermission.annotation;

import online.yueyun.datapermission.config.DataPermissionAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用数据权限功能注解
 * 在SpringBoot启动类上添加此注解以启用数据权限功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DataPermissionAutoConfiguration.class)
public @interface EnableDataPermission {
} 