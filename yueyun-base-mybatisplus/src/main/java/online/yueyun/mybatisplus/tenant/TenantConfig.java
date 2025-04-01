package online.yueyun.mybatisplus.tenant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 租户配置
 */
public class TenantConfig {
    /**
     * 忽略租户过滤的表
     */
    public static final Set<String> ignoreTables = new HashSet<>(Arrays.asList(
        "sys_config",
        "sys_dict",
        "sys_dict_data",
        "sys_menu",
        "sys_role",
        "sys_user",
        "sys_user_role"
    ));
} 