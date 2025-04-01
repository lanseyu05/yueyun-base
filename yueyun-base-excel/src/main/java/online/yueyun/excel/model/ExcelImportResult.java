package online.yueyun.excel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Excel导入结果
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResult<T> {

    /**
     * 成功导入的数据列表
     */
    private List<T> successList;

    /**
     * 导入失败的错误信息列表
     */
    private List<String> errorList;

    /**
     * 获取总行数
     */
    public int getTotalRows() {
        return (successList != null ? successList.size() : 0) + 
               (errorList != null ? errorList.size() : 0);
    }

    /**
     * 获取成功行数
     */
    public int getSuccessRows() {
        return successList != null ? successList.size() : 0;
    }

    /**
     * 获取失败行数
     */
    public int getErrorRows() {
        return errorList != null ? errorList.size() : 0;
    }

    /**
     * 是否全部导入成功
     */
    public boolean isAllSuccess() {
        return errorList == null || errorList.isEmpty();
    }
} 