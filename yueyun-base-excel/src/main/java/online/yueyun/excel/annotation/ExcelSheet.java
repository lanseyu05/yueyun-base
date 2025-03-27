package online.yueyun.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel工作表注解
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelSheet {

    /**
     * 工作表名称
     */
    String name() default "";

    /**
     * 头部行数
     */
    int headRowNumber() default 1;

    /**
     * 最大行数
     */
    int maxRows() default 10000;
} 