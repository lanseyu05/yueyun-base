# RabbitMQ 消息队列接入指南

## 一、最小化接入

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-mq</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加基础配置：

```yaml
yueyun:
  mq:
    type: rabbitmq
    enabled: true
    rabbit-mq:
      enabled: true
      host: localhost
      port: 5672
      username: guest
      password: guest
      virtual-host: /
      default-topic: your-topic
```

### 3. 代码使用

```java
@Autowired
private MessageService messageService;

// 发送消息
messageService.send("your-topic", "your-key", "your-message");

// 异步发送消息
messageService.sendAsync("your-topic", "your-key", "your-message")
    .thenAccept(success -> {
        if (success) {
            System.out.println("消息发送成功");
        } else {
            System.out.println("消息发送失败");
        }
    });

// 订阅消息
messageService.subscribe("your-topic", "your-group", String.class, message -> {
    System.out.println("收到消息: " + message);
});
```

## 二、高级配置

### 1. 连接配置

```yaml
yueyun:
  mq:
    rabbit-mq:
      connection:
        timeout: 60000              # 连接超时时间
        requested-heartbeat: 60     # 心跳间隔
        connection-timeout: 60000   # 连接超时时间
        cache:
          size: 25                 # 连接缓存大小
          mode: CHANNEL            # 缓存模式
```

效果说明：
- `timeout`: 连接超时时间
- `requested-heartbeat`: 心跳间隔，保持连接活跃
- `connection-timeout`: 连接建立超时时间
- `cache.size`: 连接缓存大小
- `cache.mode`: 缓存模式，CHANNEL模式更高效

### 2. 生产者配置

```yaml
yueyun:
  mq:
    rabbit-mq:
      publisher:
        confirms: true             # 发布确认
        returns: true              # 发布退回
        mandatory: true            # 强制消息
        batch-size: 100           # 批量发送大小
        compression: true          # 消息压缩
```

效果说明：
- `confirms`: 启用发布确认，确保消息到达交换机
- `returns`: 启用发布退回，处理无法路由的消息
- `mandatory`: 强制消息，确保消息被路由
- `batch-size`: 批量发送大小，提高吞吐量
- `compression`: 启用消息压缩，减少网络传输量

### 3. 消费者配置

```yaml
yueyun:
  mq:
    rabbit-mq:
      consumer:
        prefetch: 1                # 预取数量
        concurrency: 1             # 并发消费者数
        max-concurrency: 5         # 最大并发数
        retry:
          enabled: true           # 启用重试
          max-attempts: 3         # 最大重试次数
          initial-interval: 1000  # 初始重试间隔
          multiplier: 2.0         # 重试间隔倍数
          max-interval: 10000     # 最大重试间隔
```

效果说明：
- `prefetch`: 预取消息数量，影响消费速度
- `concurrency`: 并发消费者数，提高处理能力
- `max-concurrency`: 最大并发数，控制资源使用
- `retry`: 重试配置，处理消费失败的情况

### 4. 高级特性使用

1. 消息确认机制
```java
// 手动确认消息
channel.basicAck(deliveryTag, false);

// 拒绝消息并重新入队
channel.basicNack(deliveryTag, false, true);
```

2. 死信队列
```java
// 配置死信交换机
exchange.setArguments(Map.of(
    "x-dead-letter-exchange", "dlx",
    "x-dead-letter-routing-key", "dlk"
));
```

3. 延迟队列
```java
// 配置延迟交换机
exchange.setArguments(Map.of(
    "x-delayed-type", "direct",
    "x-delayed-message", "true"
));
```

4. 优先级队列
```java
// 配置优先级队列
queue.setArguments(Map.of(
    "x-max-priority", 10
));
```

### 5. 性能优化建议

1. 连接优化
- 使用连接池
- 合理设置心跳间隔
- 启用连接缓存

2. 消息发送优化
- 使用批量发送
- 启用消息压缩
- 合理设置确认机制

3. 消息消费优化
- 合理设置预取数量
- 使用并发消费
- 配置重试机制

4. 队列优化
- 合理设置队列参数
- 使用持久化队列
- 配置死信处理

### 6. 监控指标

1. 连接指标
- 连接数
- 连接状态
- 心跳状态

2. 队列指标
- 队列长度
- 消费速率
- 消息积压量

3. 消息指标
- 发送速率
- 确认速率
- 重试次数

4. 系统指标
- CPU使用率
- 内存使用
- 网络IO
- 磁盘IO

### 7. 最佳实践

1. 消息设计
- 合理设计消息大小
- 避免消息过大
- 使用合适的消息类型

2. 队列设计
- 合理划分队列
- 避免队列过多
- 合理设置队列参数

3. 交换机设计
- 合理使用交换机类型
- 避免交换机过多
- 合理设置路由规则

4. 监控告警
- 设置关键指标监控
- 配置合适的告警阈值
- 及时处理告警信息 