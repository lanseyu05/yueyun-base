package online.yueyun.skywalking.annotation;

import java.lang.annotation.*;

/**
 * 链路追踪注解
 * 用于标记需要进行链路追踪的方法
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Trace {

    /**
     * 操作名称
     */
    String operationName() default "";

    /**
     * 是否记录参数
     */
    boolean logParameters() default true;

    /**
     * 是否记录结果
     */
    boolean logResult() default true;

    /**
     * 是否记录异常
     */
    boolean logException() default true;
} 