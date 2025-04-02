# YueYun Base Redis 模块

## 简介
`yueyun-base-redis` 是基于Spring Data Redis的增强模块，提供了更便捷的Redis操作方式和分布式锁实现。

## 主要功能

### 1. Redis操作增强
- 通用Redis操作工具类
- 对象序列化/反序列化优化
- 批量操作支持
- 管道操作支持

### 2. 分布式锁
- 基于Redisson的分布式锁实现
- 支持可重入锁
- 支持读写锁
- 支持公平锁

### 3. 缓存注解
- 自定义缓存注解
- 缓存自动更新
- 缓存自动删除
- 缓存预热

### 4. 限流功能
- 基于Redis的限流实现
- 支持多种限流策略
- 支持分布式限流

## 使用示例

### Redis操作
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void setUser(User user) {
        redisTemplate.opsForValue().set(
            "user:" + user.getId(),
            user,
            1,
            TimeUnit.HOURS
        );
    }
    
    public User getUser(Long id) {
        return (User) redisTemplate.opsForValue().get("user:" + id);
    }
}
```

### 分布式锁
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final RedissonClient redissonClient;
    
    public void createOrder(Order order) {
        RLock lock = redissonClient.getLock("order:" + order.getId());
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // 执行业务逻辑
                orderMapper.insert(order);
            }
        } finally {
            lock.unlock();
        }
    }
}
```

### 缓存注解
```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    @Cacheable(value = "product", key = "#id")
    public Product getProduct(Long id) {
        return productMapper.selectById(id);
    }
    
    @CacheEvict(value = "product", key = "#product.id")
    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }
}
```

## 配置说明

### 基础配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 10000
      lettuce:
        pool:
          max-active: 8
          max-wait: -1
          max-idle: 8
          min-idle: 0
```

### Redisson配置
```yaml
yueyun:
  redis:
    redisson:
      enabled: true
      config: |
        singleServerConfig:
          address: "redis://localhost:6379"
          password: ""
          database: 0
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- Spring Data Redis 3.2.0+
- Redisson 3.24.0+ 