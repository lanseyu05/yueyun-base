# YueYun Base Excel 模块

## 简介
`yueyun-base-excel` 是基于EasyExcel的Excel处理模块，提供了简单易用的Excel导入导出功能。

## 主要功能

### 1. Excel导出
- 单sheet导出
- 多sheet导出
- 大数据量导出
- 自定义样式
- 模板导出

### 2. Excel导入
- 单sheet导入
- 多sheet导入
- 数据校验
- 错误处理
- 导入进度

### 3. 数据转换
- 日期格式化
- 数字格式化
- 枚举转换
- 自定义转换器

### 4. 异常处理
- 导入异常处理
- 导出异常处理
- 数据校验异常
- 自定义异常

## 使用示例

### 实体类定义
```java
@Data
public class User {
    @ExcelProperty("用户ID")
    private Long id;
    
    @ExcelProperty("用户名")
    private String username;
    
    @ExcelProperty("年龄")
    private Integer age;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @ExcelProperty("状态")
    @ExcelEnum(StatusEnum.class)
    private Integer status;
}
```

### 导出示例
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final ExcelService excelService;
    
    public void exportUsers(HttpServletResponse response) {
        List<User> users = userMapper.selectList(null);
        excelService.export(
            response,
            "用户列表",
            User.class,
            users
        );
    }
    
    public void exportTemplate(HttpServletResponse response) {
        excelService.exportTemplate(
            response,
            "用户导入模板",
            User.class
        );
    }
}
```

### 导入示例
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final ExcelService excelService;
    
    public void importUsers(MultipartFile file) {
        List<User> users = excelService.importExcel(
            file,
            User.class,
            new ImportListener<User>() {
                @Override
                public void invoke(User user, AnalysisContext context) {
                    // 处理每一行数据
                    userMapper.insert(user);
                }
                
                @Override
                public void doAfterAll(AnalysisContext context) {
                    // 导入完成后的处理
                }
            }
        );
    }
}
```

## 配置说明

### 基础配置
```yaml
yueyun:
  excel:
    # 是否开启导出
    export-enabled: true
    # 是否开启导入
    import-enabled: true
    # 导出文件大小限制（MB）
    max-size: 10
    # 导入数据量限制
    max-rows: 10000
```

### 自定义转换器
```java
@Component
public class StatusConverter implements Converter<Integer, String> {
    
    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }
    
    @Override
    public Class<?> supportExcelTypeKey() {
        return String.class;
    }
    
    @Override
    public Integer convertToJavaData(String cellValue, ExcelContentProperty contentProperty,
                                   GlobalConfiguration globalConfiguration) {
        return StatusEnum.valueOf(cellValue).getCode();
    }
    
    @Override
    public String convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                   GlobalConfiguration globalConfiguration) {
        return StatusEnum.getByCode(value).getDesc();
    }
}
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- EasyExcel 3.3.3+ 