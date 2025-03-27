package online.yueyun.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel字段注解
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelField {

    /**
     * 字段名称
     */
    String name() default "";

    /**
     * 字段顺序，越小越靠前
     */
    int order() default 0;

    /**
     * 日期格式
     */
    String dateFormat() default "";

    /**
     * 数字格式化
     */
    String numberFormat() default "";

    /**
     * 宽度
     */
    int width() default -1;

    /**
     * 是否忽略该字段
     */
    boolean ignore() default false;
} 