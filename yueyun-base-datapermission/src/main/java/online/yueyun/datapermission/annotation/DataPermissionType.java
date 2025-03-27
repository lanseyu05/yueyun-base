package online.yueyun.datapermission.annotation;

/**
 * 数据权限类型枚举
 *
 * @author YueYun
 * @since 1.0.0
 */
public enum DataPermissionType {

    /**
     * 默认
     */
    DEFAULT,

    /**
     * 全部数据权限
     */
    ALL,

    /**
     * 部门数据权限
     */
    DEPT,

    /**
     * 部门及以下数据权限
     */
    DEPT_AND_CHILD,

    /**
     * 仅本人数据权限
     */
    SELF,

    /**
     * 自定义数据权限
     */
    CUSTOM
} 