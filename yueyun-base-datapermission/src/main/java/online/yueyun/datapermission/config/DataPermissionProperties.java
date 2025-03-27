package online.yueyun.datapermission.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据权限配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.datapermission")
public class DataPermissionProperties {

    /**
     * 是否启用数据权限
     */
    private boolean enabled = true;

    /**
     * 用户ID字段名
     */
    private String userIdColumn = "create_user";

    /**
     * 部门ID字段名
     */
    private String deptIdColumn = "dept_id";

    /**
     * 管理员角色编码，拥有所有权限
     */
    private String adminRoleCode = "ROLE_ADMIN";
} 