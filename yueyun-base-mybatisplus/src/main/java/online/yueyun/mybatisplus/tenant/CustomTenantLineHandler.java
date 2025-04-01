package online.yueyun.mybatisplus.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.stereotype.Component;

/**
 * 多租户处理器
 */
@Component
public class CustomTenantLineHandler implements TenantLineHandler {

    /**
     * 获取租户ID
     */
    @Override
    public Expression getTenantId() {
        // 从当前上下文获取租户ID
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("租户ID不能为空");
        }
        return new StringValue(tenantId);
    }

    /**
     * 获取租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    /**
     * 是否忽略租户过滤
     */
    @Override
    public boolean ignoreTable(String tableName) {
        // 配置不需要租户过滤的表
        return TenantConfig.ignoreTables.contains(tableName);
    }
} 