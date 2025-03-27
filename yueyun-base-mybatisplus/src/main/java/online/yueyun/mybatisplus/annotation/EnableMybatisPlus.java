package online.yueyun.mybatisplus.annotation;

import online.yueyun.mybatisplus.config.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用MyBatisPlus功能注解
 * 在SpringBoot启动类上添加此注解以启用MyBatisPlus增强功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MybatisPlusAutoConfiguration.class)
public @interface EnableMybatisPlus {
} 