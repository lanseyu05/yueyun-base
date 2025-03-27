package online.yueyun.excel.service;

import online.yueyun.excel.listener.ReadListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Excel服务接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface ExcelService {

    /**
     * 导出Excel
     *
     * @param data 数据列表
     * @param clazz 数据类型
     * @param outputStream 输出流
     * @param <T> 数据类型
     */
    <T> void export(List<T> data, Class<T> clazz, OutputStream outputStream);
    
    /**
     * 导出Excel到指定的输出流，自定义Sheet名称
     *
     * @param data 数据列表
     * @param clazz 数据类型
     * @param outputStream 输出流
     * @param sheetName Sheet名称
     * @param <T> 数据类型
     */
    <T> void export(List<T> data, Class<T> clazz, OutputStream outputStream, String sheetName);

    /**
     * 导入Excel
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param <T> 数据类型
     * @return 数据列表
     */
    <T> List<T> importExcel(InputStream inputStream, Class<T> clazz);
    
    /**
     * 导入Excel，指定Sheet索引
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param sheetNo Sheet索引，从0开始
     * @param <T> 数据类型
     * @return 数据列表
     */
    <T> List<T> importExcel(InputStream inputStream, Class<T> clazz, int sheetNo);

    /**
     * 导入Excel，指定Sheet名称
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param sheetName Sheet名称
     * @param <T> 数据类型
     * @return 数据列表
     */
    <T> List<T> importExcel(InputStream inputStream, Class<T> clazz, String sheetName);

    /**
     * 异步导入Excel，使用自定义监听器处理数据
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param listener 读取监听器
     * @param <T> 数据类型
     */
    <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener);

    /**
     * 异步导入Excel，使用自定义监听器处理数据，指定Sheet索引
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param listener 读取监听器
     * @param sheetNo Sheet索引，从0开始
     * @param <T> 数据类型
     */
    <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener, int sheetNo);

    /**
     * 异步导入Excel，使用自定义监听器处理数据，指定Sheet名称
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param listener 读取监听器
     * @param sheetName Sheet名称
     * @param <T> 数据类型
     */
    <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener, String sheetName);
}