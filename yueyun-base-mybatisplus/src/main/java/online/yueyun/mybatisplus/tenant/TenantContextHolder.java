package online.yueyun.mybatisplus.tenant;

/**
 * 租户上下文持有者
 */
public class TenantContextHolder {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    /**
     * 设置租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取租户ID
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清除租户ID
     */
    public static void clear() {
        TENANT_ID.remove();
    }
} 