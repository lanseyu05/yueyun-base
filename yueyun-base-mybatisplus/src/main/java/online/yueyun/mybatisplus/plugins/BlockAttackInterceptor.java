package online.yueyun.mybatisplus.plugins;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

/**
 * 防止全表更新与删除插件
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class BlockAttackInterceptor implements InnerInterceptor {

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(ms.getBoundSql(parameter));
        String sql = mpBs.sql();
        try {
            if (sql.contains("UPDATE")) {
                // 执行 UPDATE 语句前检查是否有 WHERE 条件
                checkUpdateSql(sql);
            }
        } catch (JSQLParserException e) {
            log.error("解析SQL失败: " + e.getMessage());
        }
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 查询操作不需要处理
    }

    /**
     * 检查更新SQL是否包含WHERE条件
     *
     * @param sql SQL语句
     * @throws JSQLParserException SQL解析异常
     */
    protected void checkUpdateSql(String sql) throws JSQLParserException {
        Update update = (Update) CCJSqlParserUtil.parse(sql);
        Expression where = update.getWhere();
        if (where == null) {
            throw new MybatisPlusException("禁止执行全表更新操作");
        }
    }

    /**
     * 检查删除SQL是否包含WHERE条件
     *
     * @param sql SQL语句
     * @throws JSQLParserException SQL解析异常
     */
    protected void checkDeleteSql(String sql) throws JSQLParserException {
        Delete delete = (Delete) CCJSqlParserUtil.parse(sql);
        Expression where = delete.getWhere();
        if (where == null) {
            throw new MybatisPlusException("禁止执行全表删除操作");
        }
    }
} 