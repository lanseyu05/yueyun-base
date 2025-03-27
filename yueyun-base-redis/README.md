# Redis模块 (yueyun-base-redis)

Redis模块是YueYun基础框架的一部分，提供统一的Redis操作接口和自动配置，支持单机、哨兵和集群模式，并增强了分布式锁、缓存等功能。

## 特性

- 支持Redis单机、哨兵和集群模式
- 提供统一的Redis操作接口
- 支持自定义序列化和反序列化
- 提供分布式锁工具类
- 支持Spring Cache缓存管理
- 自动配置和条件装配
- 通过注解方式快速启用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-redis</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用Redis功能

在应用的启动类上添加`@EnableRedis`注解：

```java
@SpringBootApplication
@EnableRedis
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置Redis属性

在`application.yml`或`application.properties`中配置Redis属性：

```yaml
yueyun:
  redis:
    # 是否启用
    enabled: true
    # 模式: standalone（单机）, sentinel（哨兵）, cluster（集群）
    mode: standalone
    # 单机模式配置
    standalone:
      # 服务器地址
      host: localhost
      # 服务器端口
      port: 6379
      # 数据库索引
      database: 0
      # 密码
      password: 
    # 哨兵模式配置
    sentinel:
      # 主节点名称
      master: master
      # 哨兵节点，多个使用逗号分隔
      nodes: localhost:26379,localhost:26380,localhost:26381
      # 密码
      password: 
      # 数据库索引
      database: 0
    # 集群模式配置
    cluster:
      # 集群节点，多个使用逗号分隔
      nodes: localhost:7000,localhost:7001,localhost:7002
      # 最大重定向次数
      max-redirects: 3
      # 密码
      password: 
    # 连接池配置
    pool:
      # 最大连接数
      max-active: 8
      # 最大空闲连接数
      max-idle: 8
      # 最小空闲连接数
      min-idle: 0
      # 最大等待时间（毫秒）
      max-wait: -1
      # 空闲连接检测间隔（毫秒）
      time-between-eviction-runs: 30000
    # 缓存配置
    cache:
      # 是否启用缓存
      enabled: true
      # 全局缓存过期时间
      ttl: 30m
      # 全局缓存前缀
      key-prefix: "yueyun:cache:"
      # 是否使用缓存空值
      cache-null-values: true
```

### 4. 使用RedisService

```java
@Service
public class UserService {
    @Autowired
    private RedisService redisService;
    
    public void saveUser(User user) {
        // 设置值
        redisService.set("user:" + user.getId(), user);
        
        // 设置值并设置过期时间
        redisService.set("user:" + user.getId(), user, 30);
        
        // 哈希操作
        redisService.hSet("users", user.getId().toString(), user);
    }
    
    public User getUser(Long id) {
        // 获取值
        return redisService.get("user:" + id, User.class);
    }
    
    public void incrementUserVisits(Long id) {
        // 递增
        redisService.increment("user:visits:" + id, 1);
    }
}
```

### 5. 使用分布式锁

```java
@Service
public class OrderService {
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    public boolean createOrder(Order order) {
        String lockKey = "order:lock:" + order.getUserId();
        String lockValue = null;
        
        try {
            // 尝试获取锁
            lockValue = redisLockUtil.tryLock(lockKey);
            if (lockValue == null) {
                // 获取锁失败
                return false;
            }
            
            // 业务逻辑
            // ...
            
            return true;
        } finally {
            // 释放锁
            if (lockValue != null) {
                redisLockUtil.releaseLock(lockKey, lockValue);
            }
        }
    }
}
```

### 6. 使用缓存注解

```java
@Service
@CacheConfig(cacheNames = "products")
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Cacheable(key = "#id")
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    @CachePut(key = "#product.id")
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    @CacheEvict(key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
```

## 模块架构

```
yueyun-base-redis
├── annotation               - 注解
│   └── EnableRedis          - 启用Redis功能注解
├── config                   - 配置
│   ├── RedisProperties      - 配置属性类
│   └── RedisAutoConfiguration - 自动配置类
├── service                  - 服务接口
│   ├── RedisService         - Redis服务接口
│   └── impl
│       └── RedisServiceImpl - Redis服务实现
├── template                 - 模板
│   └── RedisTemplateWrapper - Redis模板包装类
└── util                     - 工具类
    └── RedisLockUtil        - Redis分布式锁工具类
```

## 高级用法

### 1. 使用Lua脚本

```java
@Service
public class ScriptService {
    @Autowired
    private RedisService redisService;
    
    public boolean compareAndSet(String key, Object expectedValue, Object newValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('set', KEYS[1], ARGV[2]) else return 0 end";
        
        RedisScript<Boolean> redisScript = RedisScript.of(script, Boolean.class);
        return Boolean.TRUE.equals(redisService.execute(redisScript, 
                Collections.singletonList(key), expectedValue, newValue));
    }
}
```

### 2. 使用自定义缓存管理器

```java
@Configuration
public class CustomCacheConfig {
    @Bean
    public CacheManager customCacheManager(RedisConnectionFactory connectionFactory) {
        // 创建缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("custom:");
        
        // 为不同的缓存名称设置不同的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
```

### 3. 使用Redis发布订阅

```java
@Service
public class MessageService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }
}

@Configuration
public class RedisSubscriberConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 添加订阅者
        container.addMessageListener(new OrderMessageListener(), 
                new PatternTopic("order:events"));
        
        return container;
    }
    
    // 消息监听器
    private static class OrderMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message, byte[] pattern) {
            // 处理消息
            System.out.println("Received: " + new String(message.getBody()));
        }
    }
}
```

## 注意事项

1. 根据实际使用场景选择合适的Redis模式：单机、哨兵或集群
2. 在分布式环境下使用分布式锁时，确保锁的粒度合适，避免锁粒度过大影响性能
3. 配置连接池参数时，根据应用负载和Redis服务器性能进行调优
4. 使用缓存时注意设置合理的TTL，避免缓存过期风暴和内存溢出
5. 在生产环境中，建议设置Redis密码和使用SSL连接增强安全性 