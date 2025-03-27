package online.yueyun.excel.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.excel.annotation.ExcelProperty;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@UtilityClass
public class ExcelUtils {

    /**
     * 获取类的所有标有@ExcelProperty注解的字段
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
                .filter(field -> field.isAnnotationPresent(ExcelProperty.class))
                .filter(field -> !field.getAnnotation(ExcelProperty.class).ignore())
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(ExcelProperty.class).order()))
                .collect(Collectors.toList());
    }

    /**
     * 获取字段上的ExcelProperty注解
     *
     * @param field 字段
     * @return 注解
     */
    public ExcelProperty getExcelProperty(Field field) {
        return field.getAnnotation(ExcelProperty.class);
    }

    /**
     * 获取字段名称
     *
     * @param field 字段
     * @return 字段名称
     */
    public String getFieldName(Field field) {
        ExcelProperty property = getExcelProperty(field);
        String name = property.name();
        return name.isEmpty() ? field.getName() : name;
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
                .map(ExcelUtils::getFieldName)
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