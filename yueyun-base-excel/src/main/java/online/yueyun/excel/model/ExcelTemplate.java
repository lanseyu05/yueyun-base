package online.yueyun.excel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel模板
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelTemplate {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件内容
     */
    private byte[] content;

    /**
     * 获取文件大小
     */
    public long getFileSize() {
        return content != null ? content.length : 0;
    }
} 