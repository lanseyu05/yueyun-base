# YueYun Base Common 模块

## 简介
`yueyun-base-common` 是悦芸基础组件库的核心模块，提供了各种通用工具类和基础功能。

## 主要功能

### 1. 通用响应对象
- `R<T>`: 统一响应对象，支持泛型
- `PageResult<T>`: 分页查询结果封装
- `TreeResult<T>`: 树形结构数据封装

### 2. 异常处理
- `GlobalExceptionHandler`: 全局异常处理器
- `BusinessException`: 业务异常基类
- `ValidationException`: 参数校验异常

### 3. 工具类
- `StringUtils`: 字符串工具类
- `DateUtils`: 日期时间工具类
- `FileUtils`: 文件操作工具类
- `SecurityUtils`: 安全相关工具类

### 4. 常量定义
- `CommonConstants`: 通用常量
- `SecurityConstants`: 安全相关常量
- `BusinessConstants`: 业务相关常量

## 使用示例

### 统一响应
```java
@GetMapping("/list")
public R<List<User>> list() {
    return R.ok(userService.list());
}
```

### 分页查询
```java
@GetMapping("/page")
public PageResult<User> page(PageQuery query) {
    return PageResult.ok(userService.page(query));
}
```

### 异常处理
```java
if (user == null) {
    throw new BusinessException("用户不存在");
}
```

## 配置说明
本模块无需额外配置，直接引入依赖即可使用。

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+ 