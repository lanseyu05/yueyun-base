package online.yueyun.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.excel.annotation.ExcelSheet;
import online.yueyun.excel.config.ExcelProperties;
import online.yueyun.excel.listener.ReadListener;
import online.yueyun.excel.model.ExcelImportResult;
import online.yueyun.excel.model.ExcelTemplate;
import online.yueyun.excel.service.EasyExcelService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel服务实现类（基于EasyExcel）
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
public class EasyExcelServiceImpl implements EasyExcelService {

    private final ExcelProperties properties;

    public EasyExcelServiceImpl(ExcelProperties properties) {
        this.properties = properties;
    }

    @Override
    public <T> void export(List<T> data, Class<T> clazz, OutputStream outputStream, String... sheetName) {
        try {
            ExcelWriter excelWriter = EasyExcel.write(outputStream, clazz)
                    .autoCloseStream(properties.getWrite().isAutoCloseStream())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet(getSheetName(clazz, sheetName)).build();
            excelWriter.write(data, writeSheet);
            excelWriter.finish();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public <T> void exportToResponse(List<T> data, Class<T> clazz, String fileName, HttpServletResponse response, String... sheetName) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
            
            export(data, clazz, response.getOutputStream(), sheetName);
        } catch (IOException e) {
            log.error("导出Excel到响应流失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public <T> List<T> importExcel(InputStream inputStream, Class<T> clazz, Object... sheetNoOrName) {
        try {
            List<T> result = new ArrayList<>();
            var reader = EasyExcel.read(inputStream, clazz, new PageReadListener<T>(result::addAll))
                    .autoCloseStream(properties.getRead().isAutoCloseStream())
                    .ignoreEmptyRow(properties.getImportConfig().isIgnoreEmptyRow());
            
            if (sheetNoOrName != null && sheetNoOrName.length > 0) {
                Object value = sheetNoOrName[0];
                if (value instanceof Integer) {
                    reader.sheet((Integer) value).doRead();
                } else {
                    reader.sheet(value.toString()).doRead();
                }
            } else {
                reader.sheet(0).doRead();
            }
            
            return result;
        } catch (Exception e) {
            log.error("导入Excel失败", e);
            throw new RuntimeException("导入Excel失败: " + e.getMessage());
        }
    }

    @Override
    public <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener, Object... sheetNoOrName) {
        try {
            var reader = EasyExcel.read(inputStream, clazz, new EasyExcelReadListener<>(listener, properties))
                    .autoCloseStream(properties.getRead().isAutoCloseStream())
                    .ignoreEmptyRow(properties.getImportConfig().isIgnoreEmptyRow());
            
            if (sheetNoOrName != null && sheetNoOrName.length > 0) {
                Object value = sheetNoOrName[0];
                if (value instanceof Integer) {
                    reader.sheet((Integer) value).doRead();
                } else {
                    reader.sheet(value.toString()).doRead();
                }
            } else {
                reader.sheet(0).doRead();
            }
        } catch (Exception e) {
            log.error("导入Excel失败", e);
            throw new RuntimeException("导入Excel失败: " + e.getMessage());
        }
    }

    @Override
    public <T> ExcelImportResult<T> importExcelWithResult(MultipartFile file, Class<T> clazz, Integer... sheetNo) {
        try {
            List<T> successList = new ArrayList<>();
            List<String> errorList = new ArrayList<>();
            
            EasyExcel.read(file.getInputStream(), clazz, new PageReadListener<T>(successList::addAll))
                    .autoCloseStream(properties.getRead().isAutoCloseStream())
                    .ignoreEmptyRow(properties.getImportConfig().isIgnoreEmptyRow())
                    .sheet(sheetNo != null && sheetNo.length > 0 ? sheetNo[0] : 0)
                    .doRead();
            
            return ExcelImportResult.<T>builder()
                    .successList(successList)
                    .errorList(errorList)
                    .build();
        } catch (IOException e) {
            log.error("导入Excel失败", e);
            throw new RuntimeException("导入Excel失败: " + e.getMessage());
        }
    }

    @Override
    public <T> void exportExcel(List<T> dataList, Class<T> clazz, String fileName, String... sheetName) {
        try {
            EasyExcel.write(fileName, clazz)
                    .autoCloseStream(properties.getWrite().isAutoCloseStream())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet(getSheetName(clazz, sheetName))
                    .doWrite(dataList);
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public <T> void exportExcelWithTemplate(List<T> dataList, String templatePath, String fileName, String... sheetName) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            if (!resource.exists()) {
                throw new FileNotFoundException("模板文件不存在: " + templatePath);
            }

            ExcelWriter excelWriter = EasyExcel.write(fileName)
                    .withTemplate(resource.getInputStream())
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet(getSheetName(dataList.get(0).getClass(), sheetName)).build();
            excelWriter.fill(dataList, writeSheet);
            excelWriter.finish();
        } catch (IOException e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public void fillTemplate(String templatePath, String fileName, Map<String, Object> params, String... sheetName) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            if (!resource.exists()) {
                throw new FileNotFoundException("模板文件不存在: " + templatePath);
            }

            ExcelWriter excelWriter = EasyExcel.write(fileName)
                    .withTemplate(resource.getInputStream())
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName != null && sheetName.length > 0 ? sheetName[0] : properties.getDefaultSheetName()).build();
            excelWriter.fill(params, writeSheet);
            excelWriter.finish();
        } catch (IOException e) {
            log.error("填充Excel模板失败", e);
            throw new RuntimeException("填充Excel模板失败: " + e.getMessage());
        }
    }

    @Override
    public ExcelTemplate getTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            if (!resource.exists()) {
                throw new FileNotFoundException("模板文件不存在: " + templatePath);
            }

            return ExcelTemplate.builder()
                    .fileName(templatePath.substring(templatePath.lastIndexOf("/") + 1))
                    .content(resource.getInputStream().readAllBytes())
                    .build();
        } catch (IOException e) {
            log.error("获取Excel模板失败", e);
            throw new RuntimeException("获取Excel模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取Sheet名称
     *
     * @param clazz 类
     * @param sheetName 指定的Sheet名称
     * @return Sheet名称
     */
    private String getSheetName(Class<?> clazz, String... sheetName) {
        if (sheetName != null && sheetName.length > 0) {
            return sheetName[0];
        }
        
        ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
        if (excelSheet != null && !excelSheet.name().isEmpty()) {
            return excelSheet.name();
        }
        
        return properties.getDefaultSheetName();
    }

    /**
     * EasyExcel读取监听器适配器
     */
    private static class EasyExcelReadListener<T> implements com.alibaba.excel.read.listener.ReadListener<T> {
        private final ReadListener<T> delegate;
        private final List<T> dataList;
        private final ExcelProperties properties;

        public EasyExcelReadListener(ReadListener<T> delegate, ExcelProperties properties) {
            this.delegate = delegate;
            this.properties = properties;
            this.dataList = new ArrayList<>();
        }

        @Override
        public void invoke(T data, com.alibaba.excel.context.AnalysisContext context) {
            dataList.add(data);
            if (dataList.size() >= properties.getImportConfig().getCacheSize()) {
                delegate.invoke(new ArrayList<>(dataList));
                dataList.clear();
            }
        }

        @Override
        public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
            if (!dataList.isEmpty()) {
                delegate.invoke(new ArrayList<>(dataList));
                dataList.clear();
            }
            delegate.doAfterAll();
        }

        @Override
        public void onException(Exception exception, com.alibaba.excel.context.AnalysisContext context) throws Exception {
            delegate.onException(exception, new ReadListener.ReadContext() {
                @Override
                public int getRowIndex() {
                    return context.readRowHolder().getRowIndex();
                }

                @Override
                public int getSheetIndex() {
                    return context.readSheetHolder().getSheetNo();
                }

                @Override
                public String getSheetName() {
                    return context.readSheetHolder().getSheetName();
                }

                @Override
                public Integer getTotalRows() {
                    return context.readSheetHolder().getTotal();
                }
            });
        }
    }
} 