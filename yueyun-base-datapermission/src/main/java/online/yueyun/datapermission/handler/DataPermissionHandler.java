package online.yueyun.datapermission.handler;

import net.sf.jsqlparser.expression.Expression;
import online.yueyun.datapermission.annotation.DataPermission;

/**
 * 数据权限处理器接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface DataPermissionHandler {

    /**
     * 获取数据权限SQL片段
     *
     * @param where 当前WHERE条件
     * @param dataPermission 数据权限注解
     * @return 新的WHERE条件
     */
    Expression getSqlSegment(Expression where, DataPermission dataPermission);

    /**
     * 是否忽略数据权限
     *
     * @return 是否忽略
     */
    default boolean ignorePermission() {
        return false;
    }
} 