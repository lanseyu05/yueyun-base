package online.yueyun.job.annotation;

import java.lang.annotation.*;

/**
 * 启用XXL-JOB定时任务功能注解
 * 在SpringBoot启动类上添加此注解以启用XXL-JOB定时任务功能
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Import(XxlJobAutoConfiguration.class)
public @interface EnableXxlJob {
} 