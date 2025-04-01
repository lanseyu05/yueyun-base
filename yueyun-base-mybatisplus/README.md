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

## 1. 最小化接入方案

### 1.1 添加依赖
```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-mybatisplus</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 1.2 基础配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: online.yueyun.**.model
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 1.3 启用MyBatis-Plus配置
```java
@Configuration
@MapperScan("online.yueyun.**.mapper")
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
```

## 2. 详细进阶配置

### 2.1 实体类配置
```java
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
```

### 2.2 自动填充配置
```java
@Component
public class MybatisPlusFillHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 2.3 多租户配置
```java
@Configuration
public class TenantConfig {
    @Bean
    public TenantLineHandler tenantLineHandler() {
        return new CustomTenantLineHandler();
    }
}
```

### 2.4 分页查询
```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    public IPage<User> getUserList(Page<User> page, UserQuery query) {
        return baseMapper.selectPage(page, new LambdaQueryWrapper<User>()
            .like(StringUtils.isNotBlank(query.getUsername()), User::getUsername, query.getUsername())
            .eq(query.getStatus() != null, User::getStatus, query.getStatus())
            .orderByDesc(User::getCreateTime));
    }
}
```

### 2.5 条件构造器
```java
// 查询条件
LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
    .eq(User::getStatus, 1)
    .like(User::getUsername, "test")
    .in(User::getId, Arrays.asList(1, 2, 3))
    .orderByDesc(User::getCreateTime);

// 更新条件
LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
    .eq(User::getId, 1)
    .set(User::getStatus, 0);
```

### 2.6 批量操作
```java
// 批量插入
List<User> userList = new ArrayList<>();
saveBatch(userList);

// 批量更新
updateBatchById(userList);

// 批量删除
removeByIds(Arrays.asList(1, 2, 3));
```

### 2.7 性能分析
```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor interceptor = new PerformanceInterceptor();
        interceptor.setMaxTime(1000);
        return interceptor;
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