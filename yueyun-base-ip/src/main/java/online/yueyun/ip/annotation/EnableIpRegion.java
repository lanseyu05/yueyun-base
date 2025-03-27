package online.yueyun.ip.annotation;

import online.yueyun.ip.config.IpRegionAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用IP地址检索功能注解
 * 在SpringBoot启动类上添加此注解以启用IP地址检索功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(IpRegionAutoConfiguration.class)
public @interface EnableIpRegion {
} 