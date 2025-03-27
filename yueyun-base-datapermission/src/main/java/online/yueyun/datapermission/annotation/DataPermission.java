package online.yueyun.datapermission.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于标记需要进行数据权限控制的方法
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 数据权限资源标识
     */
    String resource() default "";

    /**
     * 数据权限类型
     */
    DataPermissionType type() default DataPermissionType.DEFAULT;

    /**
     * 是否启用
     */
    boolean enabled() default true;
    
    /**
     * 表别名
     */
    String tableAlias() default "";
} 