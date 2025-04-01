package online.yueyun.excel.annotation;

import online.yueyun.excel.config.ExcelConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用Excel功能注解
 * 在SpringBoot启动类上添加此注解以启用Excel导入导出功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ExcelConfiguration.class)
public @interface EnableExcel {
} 