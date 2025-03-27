# MyBatisPlus模块 (yueyun-base-mybatisplus)

MyBatisPlus模块是YueYun基础框架的一部分，基于MyBatis-Plus扩展开发，提供更便捷、更强大的ORM能力，包括自动填充、分页、多租户、防全表更新与删除等增强功能。

## 特性

- 无侵入：对现有代码无侵入，只做增强不做改变
- 自动填充：支持创建时间、更新时间、创建人、更新人等字段自动填充
- 分页增强：简化分页操作，支持多种数据库方言
- 防止误操作：内置防止全表更新与删除插件
- 乐观锁：支持乐观锁插件，防止并发更新问题
- 扩展接口：提供扩展的BaseService，增强常用操作
- 通过注解方式快速启用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-mybatisplus</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用MyBatisPlus功能

在应用的启动类上添加`@EnableMybatisPlus`注解：

```java
@SpringBootApplication
@EnableMybatisPlus
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置MyBatisPlus属性

在`application.yml`或`application.properties`中配置MyBatisPlus属性：

```yaml
yueyun:
  mybatis-plus:
    # 是否启用
    enabled: true
    # 是否启用SQL打印
    sql-log: false
    # 是否启用性能分析插件（生产环境建议关闭）
    performance-interceptor: false
    # 是否启用乐观锁插件
    optimistic-lock: true
    # 是否启用防止全表更新与删除插件
    block-attack: true
    # 字段填充配置
    field-fill:
      # 是否启用字段自动填充
      enabled: true
      # 创建人字段名
      create-user-field: createUser
      # 创建时间字段名
      create-time-field: createTime
      # 更新人字段名
      update-user-field: updateUser
      # 更新时间字段名
      update-time-field: updateTime
    # 分页配置
    pagination:
      # 是否启用分页插件
      enabled: true
      # 数据库类型
      db-type: mysql
      # 分页插件优化
      optimize-join: false
      # 最大单页限制数量，默认 1000 条，-1 不受限制
      max-limit: 1000
    # 多租户配置
    tenant:
      # 是否启用多租户插件
      enabled: false
      # 租户ID字段名
      tenant-id-column: tenant_id
      # 忽略表（不进行租户条件过滤的表）
      ignore-tables:
        - sys_user
        - sys_menu
      # 忽略SQL（不进行租户条件过滤的SQL）
      ignore-sqls:
```

### 4. 使用BaseEntity

所有实体类继承BaseEntity，自动获取通用字段：

```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    // createUser, createTime, updateUser, updateTime, deleted, version 继承自 BaseEntity
}
```

### 5. 使用BaseService

创建Service接口和实现类：

```java
public interface UserService extends BaseService<User> {
    // 添加自定义方法
}
```

```java
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {
    // 实现自定义方法
}
```

### 6. 高级用法示例

#### 6.1 安全操作

使用安全操作方法，支持字段校验和权限检查：

```java
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    protected void validateEntity(User user) {
        // 自定义字段校验逻辑
        if (user.getUsername() == null || user.getUsername().length() < 3) {
            throw new IllegalArgumentException("用户名不能少于3个字符");
        }
    }
    
    @Override
    protected void checkPermission(Serializable id) {
        // 自定义权限检查逻辑
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException("无权删除用户");
        }
    }
}
```

#### 6.2 获取有序列表

按ID顺序获取实体列表：

```java
List<Long> userIds = Arrays.asList(5L, 1L, 3L);
List<User> orderedUsers = userService.listByIdsOrdered(userIds);
// 返回的用户列表顺序与userIds顺序相同：5, 1, 3
```

#### 6.3 获取Map

将实体列表转换为映射：

```java
// 将用户列表转换为 ID -> 用户名 的映射
Map<Long, String> userMap = userService.getMap("id", "username");

// 带条件的映射
QueryWrapper<User> wrapper = new QueryWrapper<User>().eq("status", 1);
Map<Long, String> activeUserMap = userService.getMap("id", "username", wrapper);
```

#### 6.4 多租户支持

自定义多租户实现：

```java
@Component
public class CustomTenantHandler implements TenantHandler {
    
    @Override
    public Long getTenantId() {
        // 从当前登录用户或请求上下文中获取租户ID
        return SecurityContextHolder.getContext().getTenantId();
    }
    
    @Override
    public boolean ignoreTable(String tableName) {
        // 自定义忽略表规则
        return "sys_config".equals(tableName) || "sys_dict".equals(tableName);
    }
}
```

## 模块架构

```
yueyun-base-mybatisplus
├── annotation              - 注解
│   └── EnableMybatisPlus   - 启用MyBatisPlus功能注解
├── config                  - 配置
│   ├── MybatisPlusProperties      - 配置属性类
│   └── MybatisPlusAutoConfiguration - 自动配置类
├── handler                 - 处理器
│   └── MybatisPlusFillHandler     - 字段自动填充处理器
├── model                   - 模型
│   └── BaseEntity          - 基础实体类
├── plugins                 - 插件
│   └── BlockAttackInterceptor     - 防全表更新与删除插件
├── service                 - 服务
│   ├── BaseService         - 基础服务接口
│   └── impl
│       └── BaseServiceImpl - 基础服务实现
└── tenant                  - 多租户
    ├── TenantHandler       - 多租户处理器接口
    └── TenantLineHandler   - 多租户条件处理器
```

## 注意事项

1. 使用防止全表更新与删除插件时，确保更新和删除操作都有WHERE条件
2. 乐观锁使用需要实体类中有@Version注解的字段
3. 多租户模式下需要表中有对应的租户ID字段
4. 自动填充功能依赖于实体类中有对应的@TableField(fill = FieldFill.XXX)注解
5. 为获得最佳性能，根据实际情况配置连接池和SQL打印等参数 