package online.yueyun.datapermission.interceptor;

import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import online.yueyun.datapermission.annotation.DataPermission;
import online.yueyun.datapermission.handler.DataPermissionHandler;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.Map;

/**
 * 数据权限拦截器
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@AllArgsConstructor
public class DataPermissionInterceptor extends JsqlParserSupport implements InnerInterceptor {

    /**
     * 数据权限处理器
     */
    private final DataPermissionHandler dataPermissionHandler;

    /**
     * 查询拦截
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {

        // 获取当前执行的方法上的数据权限注解
        DataPermission dataPermission = getDataPermission(ms);
        if (dataPermission == null || !dataPermission.enabled()) {
            return;
        }

        // 解析SQL并处理数据权限
        PluginUtils.processSelect(boundSql, connection -> {
            String targetSql = boundSql.getSql();
            try {
                targetSql = parserSingle(targetSql, dataPermission);
            } catch (Exception e) {
                log.error("数据权限处理异常", e);
            }
            return targetSql;
        });
    }

    /**
     * 解析Select语句
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        DataPermission dataPermission = (DataPermission) obj;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        // 调用数据权限处理器获取新的WHERE条件
        Expression expression = dataPermissionHandler.getSqlSegment(where, dataPermission);
        if (expression != null) {
            plainSelect.setWhere(expression);
        }
    }

    /**
     * 获取数据权限注解
     */
    private DataPermission getDataPermission(MappedStatement mappedStatement) {
        String id = mappedStatement.getId();
        try {
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            Class<?> clazz = Class.forName(className);
            
            // 优先取方法上的注解
            try {
                java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName);
                if (method.isAnnotationPresent(DataPermission.class)) {
                    return method.getAnnotation(DataPermission.class);
                }
            } catch (NoSuchMethodException e) {
                // 可能存在重载方法，忽略异常
            }
            
            // 取类上的注解
            if (clazz.isAnnotationPresent(DataPermission.class)) {
                return clazz.getAnnotation(DataPermission.class);
            }
        } catch (Exception e) {
            log.error("获取数据权限注解失败", e);
        }
        return null;
    }

    /**
     * 内部工具类
     */
    private static class PluginUtils {
        interface ProcessFunction {
            String process(java.sql.Connection connection) throws Exception;
        }

        public static void processSelect(BoundSql boundSql, ProcessFunction function) {
            try {
                String newSql = function.process(null);
                // 通过反射修改BoundSql中的sql字段
                java.lang.reflect.Field field = boundSql.getClass().getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, newSql);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
} 