# YueYun Excel 模块

基于 EasyExcel 的 Excel 导入导出工具模块，提供简单易用的 Excel 操作接口。

## 功能特性

- 支持 Excel 导入导出
- 支持自定义模板
- 支持大数据量处理
- 支持自定义监听器
- 支持异步处理
- 支持自定义样式
- 支持多 Sheet 操作
- 支持自定义字段映射

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-excel</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启用 Excel 功能

在启动类上添加 `@EnableExcel` 注解：

```java
@SpringBootApplication
@EnableExcel
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. 配置 Excel 属性

在 `application.yml` 中添加配置：

```yaml
yueyun:
  excel:
    default-sheet-name: Sheet1
    import:
      cache-size: 1000
      ignore-empty-row: true
    read:
      auto-close-stream: true
    write:
      auto-close-stream: true
```

### 4. 创建 Excel 实体类

```java
@Data
@ExcelSheet(name = "用户信息")
public class UserExcel {
    @ExcelField(name = "用户ID", order = 1)
    private Long id;
    
    @ExcelField(name = "用户名", order = 2)
    private String username;
    
    @ExcelField(name = "年龄", order = 3, numberFormat = "0")
    private Integer age;
    
    @ExcelField(name = "生日", order = 4, dateFormat = "yyyy-MM-dd")
    private Date birthday;
    
    @ExcelField(name = "状态", order = 5)
    private String status;
}
```

### 5. 使用 Excel 服务

```java
@RestController
@RequestMapping("/api/excel")
public class ExcelController {
    
    @Autowired
    private EasyExcelService excelService;
    
    /**
     * 导出 Excel
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<UserExcel> dataList = getUserList();
        excelService.exportToResponse(dataList, UserExcel.class, "用户列表.xlsx", response);
    }
    
    /**
     * 导入 Excel
     */
    @PostMapping("/import")
    public ExcelImportResult<UserExcel> importExcel(@RequestParam("file") MultipartFile file) {
        return excelService.importExcelWithResult(file, UserExcel.class);
    }
    
    /**
     * 使用监听器导入 Excel
     */
    @PostMapping("/import-with-listener")
    public void importWithListener(@RequestParam("file") MultipartFile file) {
        excelService.importExcelWithListener(
            file.getInputStream(),
            UserExcel.class,
            new ReadListener<UserExcel>() {
                @Override
                public void invoke(List<UserExcel> dataList) {
                    // 处理数据
                    processData(dataList);
                }
                
                @Override
                public void doAfterAll() {
                    // 完成后的操作
                    log.info("导入完成");
                }
                
                @Override
                public void onException(Exception e, ReadContext context) {
                    // 异常处理
                    log.error("导入异常，行号：{}", context.getRowIndex(), e);
                }
            }
        );
    }
    
    /**
     * 使用模板导出 Excel
     */
    @GetMapping("/export-with-template")
    public void exportWithTemplate(HttpServletResponse response) throws IOException {
        List<UserExcel> dataList = getUserList();
        excelService.exportExcelWithTemplate(
            dataList,
            "templates/user.xlsx",
            "用户列表.xlsx",
            response
        );
    }
    
    /**
     * 填充 Excel 模板
     */
    @GetMapping("/fill-template")
    public void fillTemplate(HttpServletResponse response) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("title", "用户统计");
        params.put("total", 100);
        params.put("date", new Date());
        
        excelService.fillTemplate(
            "templates/statistics.xlsx",
            "统计报表.xlsx",
            params,
            response
        );
    }
}
```

## 注解说明

### @EnableExcel

启用 Excel 功能，需要在启动类上添加此注解。

### @ExcelSheet

用于指定 Excel 工作表名称。

```java
@ExcelSheet(name = "用户信息")
public class UserExcel {
    // ...
}
```

### @ExcelField

用于配置 Excel 字段属性。

```java
@ExcelField(
    name = "用户名",           // 字段名称
    order = 1,               // 排序
    dateFormat = "yyyy-MM-dd", // 日期格式
    numberFormat = "0.00",    // 数字格式
    width = 20,              // 列宽
    ignore = false           // 是否忽略
)
private String username;
```

## 配置说明

### 基础配置

```yaml
yueyun:
  excel:
    default-sheet-name: Sheet1  # 默认工作表名称
```

### 导入配置

```yaml
yueyun:
  excel:
    import:
      cache-size: 1000        # 缓存大小
      ignore-empty-row: true  # 是否忽略空行
```

### 读取配置

```yaml
yueyun:
  excel:
    read:
      auto-close-stream: true  # 是否自动关闭流
```

### 写入配置

```yaml
yueyun:
  excel:
    write:
      auto-close-stream: true  # 是否自动关闭流
```

## 最佳实践

### 1. 大数据量处理

对于大数据量的 Excel 导入导出，建议使用监听器方式：

```java
excelService.importExcelWithListener(
    file.getInputStream(),
    UserExcel.class,
    new ReadListener<UserExcel>() {
        @Override
        public void invoke(List<UserExcel> dataList) {
            // 分批处理数据
            processBatch(dataList);
        }
    }
);
```

### 2. 自定义样式

可以通过 EasyExcel 的样式处理器来自定义 Excel 样式：

```java
EasyExcel.write(fileName, UserExcel.class)
    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
    .registerWriteHandler(new CustomStyleHandler())
    .sheet("用户列表")
    .doWrite(dataList);
```

### 3. 错误处理

建议在监听器中实现错误处理：

```java
@Override
public void onException(Exception e, ReadContext context) {
    log.error("导入异常，行号：{}，Sheet：{}", 
        context.getRowIndex(), 
        context.getSheetName(), 
        e
    );
    // 记录错误信息
    errorList.add(String.format("第%d行数据异常：%s", 
        context.getRowIndex(), 
        e.getMessage()
    ));
}
```

### 4. 模板使用

对于复杂的 Excel 导出，建议使用模板：

```java
// 1. 准备模板文件
// 2. 准备数据
Map<String, Object> params = new HashMap<>();
params.put("title", "用户统计");
params.put("dataList", userList);
// 3. 填充模板
excelService.fillTemplate("templates/report.xlsx", "统计报表.xlsx", params);
```

## 注意事项

1. 大数据量导入时，建议使用监听器方式，避免内存溢出
2. 导出时注意及时关闭流，避免资源泄露
3. 使用模板时，确保模板文件存在且格式正确
4. 注意处理文件名编码，避免中文乱码
5. 建议在监听器中实现错误处理，记录异常信息
6. 对于敏感数据，注意在导出时进行脱敏处理

## 常见问题

### 1. 中文乱码

导出时设置正确的响应头：

```java
response.setContentType("application/vnd.ms-excel");
response.setCharacterEncoding("utf-8");
String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
    .replaceAll("\\+", "%20");
response.setHeader("Content-disposition", 
    "attachment;filename*=utf-8''" + encodedFileName);
```

### 2. 内存溢出

使用监听器分批处理数据：

```java
@ExcelField
public void invoke(List<UserExcel> dataList) {
    // 处理一批数据
    processBatch(dataList);
    // 清理内存
    dataList.clear();
}
```

### 3. 日期格式

使用 `@ExcelField` 注解指定日期格式：

```java
@ExcelField(dateFormat = "yyyy-MM-dd HH:mm:ss")
private Date createTime;
```

### 4. 数字格式

使用 `@ExcelField` 注解指定数字格式：

```java
@ExcelField(numberFormat = "0.00")
private BigDecimal amount;
```

## 更新日志

### 1.0.0

- 初始版本发布
- 支持基本的 Excel 导入导出功能
- 支持自定义模板
- 支持大数据量处理
- 支持自定义监听器
- 支持异步处理
- 支持自定义样式
- 支持多 Sheet 操作
- 支持自定义字段映射 