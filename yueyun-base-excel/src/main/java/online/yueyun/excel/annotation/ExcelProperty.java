package online.yueyun.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel属性注解
 * 用于标记导入导出Excel的字段属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelProperty {

    /**
     * 列名
     */
    String name() default "";

    /**
     * 列顺序，值越小越靠前
     */
    int order() default Integer.MAX_VALUE;

    /**
     * 日期格式，如: yyyy-MM-dd
     */
    String dateFormat() default "";

    /**
     * 是否忽略该字段
     */
    boolean ignore() default false;

    /**
     * 宽度
     */
    int width() default 0;
} 