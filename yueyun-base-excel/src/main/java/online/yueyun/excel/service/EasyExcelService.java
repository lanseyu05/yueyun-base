package online.yueyun.excel.service;

import jakarta.servlet.http.HttpServletResponse;
import online.yueyun.excel.listener.ReadListener;
import online.yueyun.excel.model.ExcelImportResult;
import online.yueyun.excel.model.ExcelTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel服务接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface EasyExcelService {
    
    /**
     * 导出Excel到输出流
     *
     * @param data 数据列表
     * @param clazz 数据类型
     * @param outputStream 输出流
     * @param sheetName Sheet名称（可选）
     * @param <T> 数据类型
     */
    <T> void export(List<T> data, Class<T> clazz, OutputStream outputStream, String... sheetName);

    /**
     * 导出Excel到响应流
     *
     * @param data 数据列表
     * @param clazz 数据类型
     * @param fileName 文件名
     * @param response HTTP响应
     * @param sheetName Sheet名称（可选）
     * @param <T> 数据类型
     */
    <T> void exportToResponse(List<T> data, Class<T> clazz, String fileName, HttpServletResponse response, String... sheetName);

    /**
     * 导入Excel
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param sheetNoOrName Sheet序号或名称（可选）
     * @param <T> 数据类型
     * @return 数据列表
     */
    <T> List<T> importExcel(InputStream inputStream, Class<T> clazz, Object... sheetNoOrName);

    /**
     * 使用监听器导入Excel
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param listener 监听器
     * @param sheetNoOrName Sheet序号或名称（可选）
     * @param <T> 数据类型
     */
    <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener, Object... sheetNoOrName);

    /**
     * 导入Excel并返回结果
     *
     * @param file 文件
     * @param clazz 数据类型
     * @param sheetNo Sheet序号（可选）
     * @param <T> 数据类型
     * @return 导入结果
     */
    <T> ExcelImportResult<T> importExcelWithResult(MultipartFile file, Class<T> clazz, Integer... sheetNo);

    /**
     * 导出Excel到文件
     *
     * @param dataList 数据列表
     * @param clazz 数据类型
     * @param fileName 文件名
     * @param sheetName Sheet名称（可选）
     * @param <T> 数据类型
     */
    <T> void exportExcel(List<T> dataList, Class<T> clazz, String fileName, String... sheetName);

    /**
     * 使用模板导出Excel
     *
     * @param dataList 数据列表
     * @param templatePath 模板路径
     * @param fileName 文件名
     * @param sheetName Sheet名称（可选）
     * @param <T> 数据类型
     */
    <T> void exportExcelWithTemplate(List<T> dataList, String templatePath, String fileName, String... sheetName);

    /**
     * 填充Excel模板
     *
     * @param templatePath 模板路径
     * @param fileName 文件名
     * @param params 参数
     * @param sheetName Sheet名称（可选）
     */
    void fillTemplate(String templatePath, String fileName, Map<String, Object> params, String... sheetName);

    /**
     * 获取Excel模板
     *
     * @param templatePath 模板路径
     * @return 模板对象
     */
    ExcelTemplate getTemplate(String templatePath);
} 