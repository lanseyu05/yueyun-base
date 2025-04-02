# YueYun Base SkyWalking 模块

## 简介
`yueyun-base-skywalking` 是基于Apache SkyWalking的链路追踪模块，提供了分布式系统的性能监控和问题诊断功能。

## 主要功能

### 1. 链路追踪
- 分布式调用链路追踪
- 方法级性能分析
- 慢查询分析
- 异常追踪
- 自定义追踪点

### 2. 性能监控
- JVM监控
- 线程监控
- 内存监控
- GC监控
- 系统资源监控

### 3. 告警管理
- 性能告警
- 异常告警
- 自定义告警规则
- 告警通知
- 告警历史

### 4. 可视化分析
- 服务拓扑图
- 调用链路图
- 性能分析图
- 告警分析图
- 自定义图表

## 使用示例

### 基础配置
```java
@Configuration
@RequiredArgsConstructor
public class SkyWalkingConfig {
    
    @Value("${skywalking.service-name}")
    private String serviceName;
    
    @Value("${skywalking.sample-rate}")
    private double sampleRate;
    
    @Bean
    public Tracer tracer() {
        return new Tracer(serviceName, sampleRate);
    }
}
```

### 自定义追踪
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final Tracer tracer;
    
    public Order createOrder(Order order) {
        // 创建追踪span
        Span span = tracer.newSpan("createOrder");
        try {
            // 添加标签
            span.tag("orderId", order.getId());
            span.tag("amount", order.getAmount());
            
            // 执行业务逻辑
            orderMapper.insert(order);
            
            // 记录事件
            span.log("订单创建成功");
            
            return order;
        } catch (Exception e) {
            // 记录错误
            span.error(e);
            throw e;
        } finally {
            // 结束span
            span.finish();
        }
    }
}
```

### 方法追踪注解
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    @Trace
    public User getUser(Long id) {
        return userMapper.selectById(id);
    }
    
    @Trace(operationName = "批量查询用户")
    public List<User> getUsers(List<Long> ids) {
        return userMapper.selectBatchIds(ids);
    }
}
```

## 配置说明

### 基础配置
```yaml
skywalking:
  service-name: yueyun-service
  sample-rate: 1.0
  agent:
    collector:
      backend-service: localhost:11800
    logging:
      level: INFO
    buffer:
      size: 300
```

### 性能配置
```yaml
yueyun:
  skywalking:
    # 是否开启性能监控
    performance-enabled: true
    # 性能数据采集间隔（秒）
    collect-interval: 60
    # 性能数据保留时间（天）
    retention-days: 7
    # 告警阈值配置
    alert:
      # 响应时间阈值（毫秒）
      response-time-threshold: 1000
      # 错误率阈值（百分比）
      error-rate-threshold: 1.0
      # 内存使用率阈值（百分比）
      memory-usage-threshold: 80.0
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- SkyWalking Agent 8.12.0+
- SkyWalking OAP Server 8.12.0+ 