# RocketMQ 消息队列接入指南

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
    type: rocketmq
    enabled: true
    rocket-mq:
      enabled: true
      name-server: localhost:9876
      producer-group: your-producer-group
      consumer-group: your-consumer-group
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

### 1. 生产者配置

```yaml
yueyun:
  mq:
    rocket-mq:
      producer:
        send-message-timeout: 3000           # 发送超时时间
        compress-msg-body-over-howmuch: 4096 # 消息压缩阈值
        max-retry-count: 3                   # 最大重试次数
        retry-times-when-send-failed: 2      # 发送失败重试次数
        retry-times-when-send-async-failed: 2 # 异步发送失败重试次数
```

效果说明：
- `send-message-timeout`: 消息发送超时时间
- `compress-msg-body-over-howmuch`: 消息压缩阈值，超过此大小的消息会被压缩
- `max-retry-count`: 最大重试次数，提高消息可靠性
- `retry-times-when-send-failed`: 同步发送失败重试次数
- `retry-times-when-send-async-failed`: 异步发送失败重试次数

### 2. 消费者配置

```yaml
yueyun:
  mq:
    rocket-mq:
      consumer:
        pull-batch-size: 32                  # 批量拉取大小
        consume-message-batch-max-size: 1    # 批量消费大小
        consume-thread-min: 20               # 最小消费线程数
        consume-thread-max: 64               # 最大消费线程数
        pull-interval: 0                     # 拉取间隔
```

效果说明：
- `pull-batch-size`: 批量拉取消息的大小
- `consume-message-batch-max-size`: 批量消费消息的最大数量
- `consume-thread-min`: 最小消费线程数
- `consume-thread-max`: 最大消费线程数
- `pull-interval`: 拉取消息的时间间隔

### 3. 高级特性使用

1. 事务消息
```java
// 发送事务消息
messageService.sendTransactionMessage(topic, key, message, arg, executor);
```

2. 延迟消息
```java
// 发送延迟消息
messageService.sendDelayMessage(topic, key, message, delayLevel);
```

3. 顺序消息
```java
// 发送顺序消息
messageService.sendOrderlyMessage(topic, key, message, hashKey);
```

4. 批量消息
```java
// 发送批量消息
messageService.sendBatchMessage(topic, messages);
```

### 4. 性能优化建议

1. 消息发送优化
- 合理设置批量发送大小
- 使用异步发送方式
- 适当配置重试策略

2. 消息消费优化
- 合理设置消费线程数
- 使用批量消费
- 避免频繁创建消费者实例

3. 消息压缩
- 根据消息大小设置压缩阈值
- 选择合适的压缩算法

4. 消息过滤
- 使用消息过滤功能减少无效消息
- 合理设置过滤条件

### 5. 监控指标

1. 生产者指标
- 发送TPS
- 发送延迟
- 发送失败率
- 重试次数

2. 消费者指标
- 消费TPS
- 消费延迟
- 消费失败率
- 消息积压量

3. 系统指标
- CPU使用率
- 内存使用
- 网络IO
- 磁盘IO

### 6. 最佳实践

1. 消息设计
- 合理设计消息大小
- 避免消息过大
- 使用合适的消息类型

2. 主题设计
- 合理划分主题
- 避免主题过多
- 合理设置主题权限

3. 消费组设计
- 合理划分消费组
- 避免消费组过多
- 合理设置消费组权限

4. 监控告警
- 设置关键指标监控
- 配置合适的告警阈值
- 及时处理告警信息 