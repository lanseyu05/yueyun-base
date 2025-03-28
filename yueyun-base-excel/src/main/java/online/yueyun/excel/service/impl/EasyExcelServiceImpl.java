package online.yueyun.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.excel.annotation.ExcelSheet;
import online.yueyun.excel.config.ExcelProperties;
import online.yueyun.excel.service.ExcelService;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel服务实现类（基于EasyExcel）
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class EasyExcelServiceImpl implements ExcelService {

    private final ExcelProperties properties;

    public EasyExcelServiceImpl(ExcelProperties properties) {
        this.properties = properties;
    }

    @Override
    public <T> void export(HttpServletResponse response, List<T> data, Class<T> clazz, String fileName) {
        try {
            // 检查数据量，防止OOM
            if (data != null && data.size() > properties.getWrite().getMaxSheetRows()) {
                throw new IllegalArgumentException("数据量过大，超过最大导出行数限制：" + properties.getWrite().getMaxSheetRows());
            }

            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            // 导出Excel
            export(response.getOutputStream(), data, clazz);
        } catch (IOException e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    @Override
    public <T> void export(OutputStream outputStream, List<T> data, Class<T> clazz) {
        try {
            // 获取Sheet名称
            String sheetName = properties.getDefaultSheetName();
            ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
            if (excelSheet != null && StringUtils.hasText(excelSheet.name())) {
                sheetName = excelSheet.name();
            }

            // 导出Excel
            EasyExcel.write(outputStream, clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    @Override
    public <T> List<T> importExcel(MultipartFile file, Class<T> clazz) {
        try {
            return importExcel(file.getInputStream(), clazz);
        } catch (IOException e) {
            log.error("导入Excel失败", e);
            throw new RuntimeException("导入Excel失败", e);
        }
    }

    @Override
    public <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        List<T> dataList = new ArrayList<>();
        try {
            // 获取头部行数
            int headRowNumber = 1;
            ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
            if (excelSheet != null) {
                headRowNumber = excelSheet.headRowNumber();
            }

            // 读取Excel
            EasyExcel.read(inputStream, clazz, new PageReadListener<T>(dataList::addAll, properties.getRead().getBatchSize())
                    .headRowNumber(headRowNumber)
                    .sheet()
                    .doRead();

            // 检查数据量，防止OOM
            if (dataList.size() > properties.getMaxImportRows()) {
                throw new IllegalArgumentException("数据量过大，超过最大导入行数限制：" + properties.getMaxImportRows());
            }

            return dataList;
        } catch (Exception e) {
            log.error("导入Excel失败", e);
            throw new RuntimeException("导入Excel失败", e);
        }
    }

    @Override
    public <T> void importExcelAsync(MultipartFile file, Class<T> clazz, Consumer<List<T>> consumer) {
        try {
            // 获取头部行数
            int headRowNumber = 1;
            ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
            if (excelSheet != null) {
                headRowNumber = excelSheet.headRowNumber();
            }

            // 分批读取Excel
            EasyExcel.read(file.getInputStream(), clazz, new PageReadListener<T>(consumer, 1000))
                    .headRowNumber(headRowNumber)
                    .sheet()
                    .doRead();
        } catch (IOException e) {
            log.error("异步导入Excel失败", e);
            throw new RuntimeException("异步导入Excel失败", e);
        }
    }
} 