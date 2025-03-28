package online.yueyun.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.excel.config.ExcelProperties;
import online.yueyun.excel.listener.ReadListener;
import online.yueyun.excel.service.ExcelService;
import online.yueyun.excel.util.ExcelUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel服务实现
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class ExcelServiceImpl implements ExcelService {

    private final ExcelProperties properties;

    public ExcelServiceImpl(ExcelProperties properties) {
        this.properties = properties;
    }

    @Override
    public <T> void export(List<T> data, Class<T> clazz, OutputStream outputStream) {
        export(data, clazz, outputStream, "Sheet1");
    }

    @Override
    public <T> void export(List<T> data, Class<T> clazz, OutputStream outputStream, String sheetName) {
        if (data == null || data.isEmpty()) {
            data = new ArrayList<>();
        }

        ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream, clazz)
                .autoCloseStream(properties.getWrite().isAutoCloseStream())
                ;

        // 自动调整列宽
        if (properties.getWrite().isUseDefaultStyle()) {
            writerBuilder.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
        }

        // 分批写入，防止OOM
        int maxSheetRows = properties.getWrite().getMaxSheetRows();
        if (data.size() <= maxSheetRows) {
            writerBuilder.sheet(sheetName).doWrite(data);
        } else {
            int sheetNo = 1;
            for (int i = 0; i < data.size(); i += maxSheetRows) {
                List<T> subList = data.subList(i, Math.min(i + maxSheetRows, data.size()));
                writerBuilder.sheet(sheetNo++).doWrite(subList);
            }
        }
    }

    @Override
    public <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        return importExcel(inputStream, clazz, 0);
    }

    @Override
    public <T> List<T> importExcel(InputStream inputStream, Class<T> clazz, int sheetNo) {
        List<T> result = new ArrayList<>();
        
        // 使用PageReadListener同步读取数据
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(result::addAll))
                .autoCloseStream(properties.getRead().isAutoCloseStream())
                .ignoreEmptyRow(properties.isIgnoreEmptyRow())
                .sheet(sheetNo)
                .doRead();
        
        return result;
    }

    @Override
    public <T> List<T> importExcel(InputStream inputStream, Class<T> clazz, String sheetName) {
        List<T> result = new ArrayList<>();
        
        // 使用PageReadListener同步读取数据
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(result::addAll))
                .autoCloseStream(properties.getRead().isAutoCloseStream())
                .ignoreEmptyRow(properties.isIgnoreEmptyRow())
                .sheet(sheetName)
                .doRead();
        
        return result;
    }

    @Override
    public <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener) {
        importExcelWithListener(inputStream, clazz, listener, 0);
    }

    @Override
    public <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener, int sheetNo) {
        EasyExcelReadListener<T> easyExcelListener = new EasyExcelReadListener<>(listener, properties.getRead().getBatchSize());
        
        ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream, clazz, easyExcelListener)
                .autoCloseStream(properties.getRead().isAutoCloseStream())
                .ignoreEmptyRow(properties.isIgnoreEmptyRow())
                .headRowNumber(properties.getRead().getHeadRowNumber());
        
        readerBuilder.sheet(sheetNo).doRead();
    }

    @Override
    public <T> void importExcelWithListener(InputStream inputStream, Class<T> clazz, ReadListener<T> listener, String sheetName) {
        EasyExcelReadListener<T> easyExcelListener = new EasyExcelReadListener<>(listener, properties.getRead().getBatchSize());
        
        ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream, clazz, easyExcelListener)
                .autoCloseStream(properties.getRead().isAutoCloseStream())
                .ignoreEmptyRow(properties.isIgnoreEmptyRow())
                .headRowNumber(properties.getRead().getHeadRowNumber());
        
        readerBuilder.sheet(sheetName).doRead();
    }

    /**
     * EasyExcel读取监听器适配器
     */
    private static class EasyExcelReadListener<T> implements com.alibaba.excel.read.listener.ReadListener<T> {
        private final ReadListener<T> readListener;
        private final int batchSize;
        private final List<T> dataBuffer = new ArrayList<>();

        public EasyExcelReadListener(ReadListener<T> readListener, int batchSize) {
            this.readListener = readListener;
            this.batchSize = batchSize;
        }

        @Override
        public void invoke(T data, com.alibaba.excel.context.AnalysisContext context) {
            dataBuffer.add(data);
            if (dataBuffer.size() >= batchSize) {
                readListener.invoke(new ArrayList<>(dataBuffer));
                dataBuffer.clear();
            }
        }

        @Override
        public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
            // 处理剩余的数据
            if (!dataBuffer.isEmpty()) {
                readListener.invoke(new ArrayList<>(dataBuffer));
                dataBuffer.clear();
            }
            
            readListener.doAfterAll();
        }

        @Override
        public void onException(Exception exception, com.alibaba.excel.context.AnalysisContext context) throws Exception {
            ReadListener.ReadContext readContext = new ReadListener.ReadContext() {
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
                    return context.readSheetHolder().getApproximateTotalRowNumber();
                }
            };
            
            readListener.onException(exception, readContext);
        }
    }
} 