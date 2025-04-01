# yueyun-base-common 模块

## 1. 最小化接入方案

### 1.1 添加依赖
```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 1.2 基础配置
```yaml
yueyun:
  common:
    # 是否启用全局异常处理
    global-exception-enabled: true
    # 是否启用统一响应处理
    response-enabled: true
    # 是否启用参数校验
    validation-enabled: true
```

## 2. 详细进阶配置

### 2.1 全局异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(e.getMessage());
    }
}
```

### 2.2 统一响应处理
```java
@RestController
public class TestController {
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("test");
    }
}
```

### 2.3 参数校验
```java
@Data
public class UserDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

### 2.4 工具类使用
```java
// 日期工具类
DateUtils.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");

// 字符串工具类
StringUtils.isBlank("test");

// 加密工具类
EncryptUtils.md5("password");

// 文件工具类
FileUtils.getFileExtension("test.jpg");
```

### 2.5 常量定义
```java
// 系统常量
SystemConstants.SUCCESS_CODE
SystemConstants.ERROR_CODE

// 业务常量
BusinessConstants.USER_STATUS_NORMAL
BusinessConstants.USER_STATUS_DISABLED
```

### 2.6 注解使用
```java
// 日志注解
@Log(title = "用户管理", businessType = BusinessType.INSERT)
public void addUser() {
    // 业务逻辑
}

// 数据权限注解
@DataScope(deptAlias = "d", userAlias = "u")
public List<User> selectUserList() {
    // 查询逻辑
}
``` 