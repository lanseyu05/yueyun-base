package online.yueyun.excel.util;

import lombok.experimental.UtilityClass;
import online.yueyun.excel.annotation.ExcelField;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel字段工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@UtilityClass
public class ExcelFieldUtils {

    /**
     * 获取类的所有标有@ExcelField注解的字段
     *
     * @param clazz 类
     * @return 字段列表，按照order排序
     */
    public List<Field> getExcelFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        
        // 获取当前类和父类的所有字段
        for (Class<?> cls = clazz; cls != Object.class; cls = cls.getSuperclass()) {
            fieldList.addAll(Arrays.asList(cls.getDeclaredFields()));
        }
        
        // 过滤并排序
        return fieldList.stream()
                .filter(field -> field.isAnnotationPresent(ExcelField.class))
                .filter(field -> !field.getAnnotation(ExcelField.class).ignore())
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(ExcelField.class).order()))
                .collect(Collectors.toList());
    }

    /**
     * 获取字段上的ExcelField注解
     *
     * @param field 字段
     * @return 注解
     */
    public ExcelField getExcelField(Field field) {
        return field.getAnnotation(ExcelField.class);
    }

    /**
     * 获取字段名称
     *
     * @param field 字段
     * @return 字段名称
     */
    public String getFieldName(Field field) {
        ExcelField excelField = getExcelField(field);
        String name = excelField.name();
        return name.isEmpty() ? field.getName() : name;
    }

    /**
     * 获取字段日期格式
     *
     * @param field 字段
     * @return 日期格式
     */
    public String getDateFormat(Field field) {
        ExcelField excelField = getExcelField(field);
        return excelField.dateFormat();
    }

    /**
     * 获取字段数字格式
     *
     * @param field 字段
     * @return 数字格式
     */
    public String getNumberFormat(Field field) {
        ExcelField excelField = getExcelField(field);
        return excelField.numberFormat();
    }

    /**
     * 获取字段宽度
     *
     * @param field 字段
     * @return 宽度
     */
    public int getWidth(Field field) {
        ExcelField excelField = getExcelField(field);
        return excelField.width();
    }

    /**
     * 构建表头
     *
     * @param clazz 类
     * @return 表头
     */
    public List<String> buildHeadList(Class<?> clazz) {
        List<Field> fields = getExcelFields(clazz);
        return fields.stream()
                .map(ExcelFieldUtils::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * 构建表头行
     *
     * @param clazz 类
     * @return 表头行
     */
    public List<List<String>> buildHeadRowList(Class<?> clazz) {
        List<String> headList = buildHeadList(clazz);
        return headList.stream()
                .map(head -> Collections.singletonList(head))
                .collect(Collectors.toList());
    }
} 