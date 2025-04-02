package online.yueyun.common.annotation;

import java.lang.annotation.*;

/**
 * 跳过token验证注解
 *
 * @author YueYun
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipTokenValidation {
} 