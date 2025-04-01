package online.yueyun.excel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Excel配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "yueyun.excel")
public class ExcelProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 默认Sheet名称
     */
    private String defaultSheetName = "Sheet1";

    /**
     * 上传临时目录
     */
    private String uploadTempDir = System.getProperty("java.io.tmpdir");

    /**
     * 最大上传大小（默认10MB）
     */
    private long maxUploadSize = 10 * 1024 * 1024;

    /**
     * 是否忽略空行
     */
    private boolean ignoreEmptyRow = true;

    /**
     * 写入配置
     */
    private Write write = new Write();

    /**
     * 读取配置
     */
    private Read read = new Read();

    /**
     * 导入配置
     */
    private Import importConfig = new Import();

    /**
     * 导出配置
     */
    private Export exportConfig = new Export();

    /**
     * 写入配置
     */
    @Data
    public static class Write {
        /**
         * 是否自动关闭流
         */
        private boolean autoCloseStream = true;

        /**
         * 是否使用默认样式
         */
        private boolean useDefaultStyle = true;

        /**
         * 是否自动列宽
         */
        private boolean autoColumnWidth = true;
    }

    /**
     * 读取配置
     */
    @Data
    public static class Read {
        /**
         * 是否自动关闭流
         */
        private boolean autoCloseStream = true;

        /**
         * 是否启用缓存
         */
        private boolean enableCache = true;

        /**
         * 缓存大小
         */
        private int cacheSize = 1000;

        /**
         * 最大行数
         */
        private int maxRows = 10000;

        /**
         * 是否自动转换日期
         */
        private boolean autoConvertDate = true;
    }

    /**
     * 导入配置
     */
    @Data
    public static class Import {
        /**
         * 是否启用缓存
         */
        private boolean enableCache = true;

        /**
         * 缓存大小
         */
        private int cacheSize = 1000;

        /**
         * 最大行数
         */
        private int maxRows = 10000;

        /**
         * 是否忽略空行
         */
        private boolean ignoreEmptyRow = true;

        /**
         * 是否自动转换日期
         */
        private boolean autoConvertDate = true;
    }

    /**
     * 导出配置
     */
    @Data
    public static class Export {
        /**
         * 是否启用缓存
         */
        private boolean enableCache = true;

        /**
         * 缓存大小
         */
        private int cacheSize = 1000;

        /**
         * 是否自动列宽
         */
        private boolean autoColumnWidth = true;

        /**
         * 是否使用默认样式
         */
        private boolean useDefaultStyle = true;

        /**
         * 是否使用模板
         */
        private boolean useTemplate = false;

        /**
         * 模板路径
         */
        private String templatePath;
    }
}