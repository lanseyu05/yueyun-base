# Excel操作模块 (yueyun-base-excel)

Excel操作模块是YueYun基础框架的一部分，提供了统一的Excel导入导出功能，基于Alibaba的EasyExcel实现，简化Excel操作，支持大数据量导入导出，防止OOM。

## 特性

- 简化Excel导入导出操作
- 支持通过注解配置Excel列属性
- 支持大数据量分批处理，防止OOM
- 支持同步和异步导入
- 支持自定义读取监听器
- 支持多Sheet导出
- 自动配置和条件装配
- 通过注解方式快速启用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-excel</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用Excel功能

在应用的启动类上添加`@EnableExcel`注解：

```java
@SpringBootApplication
@EnableExcel
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置Excel属性（可选）

在`application.yml`或`application.properties`中配置Excel属性：

```yaml
yueyun:
  excel:
    enabled: true
    upload-temp-dir: /tmp/excel
    max-upload-size: 10485760 # 10MB
    ignore-empty-row: true
    write:
      default-date-format: yyyy-MM-dd
      auto-close-stream: true
      use-default-style: true
      max-sheet-rows: 1000000
    read:
      default-date-format: yyyy-MM-dd
      auto-close-stream: true
      head-row-number: 1
      batch-size: 100
```

### 4. 定义数据模型

使用`@ExcelProperty`注解标记需要导入导出的字段：

```java
@Data
public class UserDto {
    
    @ExcelProperty(name = "用户ID", order = 1)
    private Long id;
    
    @ExcelProperty(name = "用户名", order = 2)
    private String username;
    
    @ExcelProperty(name = "邮箱", order = 3)
    private String email;
    
    @ExcelProperty(name = "创建时间", order = 4, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @ExcelProperty(ignore = true)
    private String password;
}
```

### 5. 导出Excel

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=users.xlsx");
        
        // 获取数据
        List<UserDto> users = userService.listAll();
        
        // 导出
        excelService.export(users, UserDto.class, response.getOutputStream());
    }
}
```

### 6. 导入Excel

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/import")
    public String importUsers(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            List<UserDto> users = excelService.importExcel(inputStream, UserDto.class);
            userService.batchSave(users);
            return "导入成功，共导入" + users.size() + "条数据";
        }
    }
}
```

### 7. 使用异步读取处理大数据量

```java
@Service
public class UserImportService {
    
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private UserRepository userRepository;
    
    public void importUsers(InputStream inputStream) {
        excelService.importExcelWithListener(
            inputStream, 
            UserDto.class,
            new ReadListener<UserDto>() {
                @Override
                public void invoke(List<UserDto> dataList) {
                    // 批量处理数据
                    userRepository.batchSave(dataList);
                }
                
                @Override
                public void doAfterAll() {
                    // 处理完成后的逻辑
                    log.info("用户导入完成");
                }
            }
        );
    }
}
```

## 模块架构

```
yueyun-base-excel
├── annotation              - 注解
│   ├── EnableExcel         - 启用Excel功能注解
│   └── ExcelProperty       - Excel属性注解
├── config                  - 配置
│   ├── ExcelProperties     - 配置属性类
│   └── ExcelAutoConfiguration - 自动配置类
├── listener                - 监听器
│   └── ReadListener        - 读取监听器接口
├── service                 - 服务
│   ├── ExcelService        - Excel服务接口
│   └── impl                - 实现类
│       └── ExcelServiceImpl - Excel服务实现
└── util                    - 工具类
    └── ExcelUtils          - Excel工具类
```

## 高级用法

### 1. 指定Sheet名称

```java
excelService.export(data, UserDto.class, outputStream, "用户数据");
```

### 2. 导入指定Sheet

```java
// 导入第一个Sheet
List<UserDto> users = excelService.importExcel(inputStream, UserDto.class, 0);

// 导入指定名称的Sheet
List<UserDto> users = excelService.importExcel(inputStream, UserDto.class, "用户数据");
```

### 3. 自定义列宽和样式

通过`@ExcelProperty`注解的`width`属性设置列宽：

```java
@ExcelProperty(name = "备注", order = 5, width = 50)
private String remark;
```

## 注意事项

1. 大数据量导入导出时，建议使用异步方式或分批处理，避免OOM
2. 导入导出字段需要使用`@ExcelProperty`注解标记
3. 日期格式化需要在`@ExcelProperty`注解中配置`dateFormat`属性
4. 敏感字段可以通过`ignore = true`属性忽略导出 