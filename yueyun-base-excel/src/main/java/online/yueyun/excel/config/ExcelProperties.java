package online.yueyun.excel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Excel配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.excel")
public class ExcelProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 上传临时目录
     */
    private String uploadTempDir = System.getProperty("java.io.tmpdir");

    /**
     * 最大上传大小（字节）
     */
    private long maxUploadSize = 10 * 1024 * 1024; // 10MB

    /**
     * 读取时是否忽略空行
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
     * 写入配置
     */
    @Data
    public static class Write {
        /**
         * 默认日期格式
         */
        private String defaultDateFormat = "yyyy-MM-dd";

        /**
         * 是否自动关闭流
         */
        private boolean autoCloseStream = true;

        /**
         * 是否使用默认样式
         */
        private boolean useDefaultStyle = true;

        /**
         * 单次写入最大行数
         */
        private int maxSheetRows = 1000000;
    }

    /**
     * 读取配置
     */
    @Data
    public static class Read {
        /**
         * 默认日期格式
         */
        private String defaultDateFormat = "yyyy-MM-dd";

        /**
         * 是否自动关闭流
         */
        private boolean autoCloseStream = true;

        /**
         * 表头行数
         */
        private int headRowNumber = 1;

        /**
         * 批量处理大小
         */
        private int batchSize = 100;
    }
}