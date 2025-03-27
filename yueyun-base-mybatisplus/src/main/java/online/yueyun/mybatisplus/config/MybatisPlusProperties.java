package online.yueyun.mybatisplus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * MyBatisPlus配置属性类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.mybatis-plus")
public class MybatisPlusProperties {

    /**
     * 是否启用MyBatisPlus
     */
    private boolean enabled = true;

    /**
     * 是否启用SQL日志
     */
    private boolean sqlLog = false;

    /**
     * 是否启用性能分析插件（开发环境使用）
     */
    private boolean performanceInterceptor = false;

    /**
     * 是否启用乐观锁插件
     */
    private boolean optimisticLock = true;

    /**
     * 是否启用防止全表更新与删除插件
     */
    private boolean blockAttack = true;

    /**
     * 字段填充配置
     */
    private FieldFill fieldFill = new FieldFill();

    /**
     * 分页配置
     */
    private Pagination pagination = new Pagination();

    /**
     * 多租户配置
     */
    private Tenant tenant = new Tenant();

    /**
     * 字段填充配置类
     */
    @Data
    public static class FieldFill {
        /**
         * 是否启用字段自动填充
         */
        private boolean enabled = true;

        /**
         * 创建人字段名
         */
        private String createUserField = "createUser";

        /**
         * 创建时间字段名
         */
        private String createTimeField = "createTime";

        /**
         * 更新人字段名
         */
        private String updateUserField = "updateUser";

        /**
         * 更新时间字段名
         */
        private String updateTimeField = "updateTime";
    }

    /**
     * 分页配置类
     */
    @Data
    public static class Pagination {
        /**
         * 是否启用分页插件
         */
        private boolean enabled = true;

        /**
         * 数据库类型
         */
        private String dbType = "mysql";

        /**
         * 是否优化分页子查询
         */
        private boolean optimizeJoin = false;

        /**
         * 单页分页条数限制
         */
        private long maxLimit = 1000;
    }

    /**
     * 多租户配置类
     */
    @Data
    public static class Tenant {
        /**
         * 是否启用多租户插件
         */
        private boolean enabled = false;

        /**
         * 租户ID列名
         */
        private String tenantIdColumn = "tenant_id";

        /**
         * 忽略的表名（不增加租户条件）
         */
        private List<String> ignoreTables = new ArrayList<>();

        /**
         * 忽略的SQL（不增加租户条件）
         */
        private List<String> ignoreSqls = new ArrayList<>();
    }
} 