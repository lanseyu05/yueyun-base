# YueYun Base MyBatis-Plus 模块

## 简介
`yueyun-base-mybatisplus` 是基于MyBatis-Plus的增强模块，提供了更强大的数据库操作功能和便捷的开发体验。

## 主要功能

### 1. 基础CRUD增强
- 通用Service基类
- 通用Mapper基类
- 分页查询优化
- 批量操作优化

### 2. 数据权限控制
- 多租户支持
- 数据过滤
- 字段加密

### 3. 审计功能
- 自动填充创建时间
- 自动填充更新时间
- 自动填充创建人
- 自动填充更新人

### 4. 性能优化
- 分页插件优化
- 动态表名支持
- 多数据源支持

## 使用示例

### 实体类定义
```java
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### Service层使用
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public PageResult<User> page(PageQuery query) {
        return PageResult.ok(baseMapper.selectPage(
            new Page<>(query.getPageNum(), query.getPageSize()),
            new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(query.getKeyword()), User::getUsername, query.getKeyword())
        ));
    }
}
```

### 数据权限示例
```java
@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加数据权限插件
        interceptor.addInnerInterceptor(new DataPermissionInterceptor());
        return interceptor;
    }
}
```

## 配置说明

### 基础配置
```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: online.yueyun.**.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 多数据源配置
```yaml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/master
          username: root
          password: root
        slave:
          url: jdbc:mysql://localhost:3306/slave
          username: root
          password: root
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- MyBatis-Plus 3.5.5+ 