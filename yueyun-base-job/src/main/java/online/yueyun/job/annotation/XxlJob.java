package online.yueyun.job.annotation;

import java.lang.annotation.*;

/**
 * XXL-JOB任务注解
 * 在方法上使用此注解来注册XXL-JOB任务处理器
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface XxlJob {

    /**
     * 任务处理器名称
     */
    String value();

    /**
     * 初始化方法
     */
    String init() default "";

    /**
     * 销毁方法
     */
    String destroy() default "";
} 