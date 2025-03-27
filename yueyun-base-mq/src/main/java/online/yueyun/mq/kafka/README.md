# Kafka 消息队列接入指南

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
    type: kafka
    enabled: true
    kafka:
      enabled: true
      bootstrap-servers: localhost:9092
      default-topic: your-topic
      default-group: your-group
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
    kafka:
      producer:
        retries: 3                    # 重试次数
        batch-size: 16384            # 批量发送大小
        buffer-memory: 33554432      # 缓冲区大小
        compression-type: snappy      # 压缩类型
        linger-ms: 1                 # 等待时间
        max-block-ms: 60000          # 最大阻塞时间
```

效果说明：
- `retries`: 消息发送失败时的重试次数，提高消息可靠性
- `batch-size`: 批量发送消息的大小，提高吞吐量
- `buffer-memory`: 发送缓冲区大小，影响内存使用
- `compression-type`: 消息压缩类型，减少网络传输量
- `linger-ms`: 等待时间，增加批量发送机会
- `max-block-ms`: 发送阻塞时间，控制超时行为

### 2. 消费者配置

```yaml
yueyun:
  mq:
    kafka:
      consumer:
        enable-auto-commit: true     # 自动提交
        auto-commit-interval-ms: 1000 # 自动提交间隔
        session-timeout-ms: 30000    # 会话超时时间
        max-poll-records: 500        # 单次拉取最大记录数
        fetch-min-bytes: 1           # 最小拉取字节数
        fetch-max-wait-ms: 500       # 最大等待时间
```

效果说明：
- `enable-auto-commit`: 是否自动提交offset，影响消息可靠性
- `auto-commit-interval-ms`: 自动提交的时间间隔
- `session-timeout-ms`: 消费者会话超时时间
- `max-poll-records`: 单次拉取的最大消息数
- `fetch-min-bytes`: 最小拉取字节数，影响响应时间
- `fetch-max-wait-ms`: 最大等待时间，影响延迟

### 3. 高级特性使用

1. 消息可靠性保证
```java
// 消息发送前会记录到数据库
MessageRecord record = messageService.createMessageRecord(topic, key, message);
```

2. 幂等性消费
```java
// 消费消息时会记录到数据库
messageConsumedService.markAsConsumed(msgId, group);
```

3. 自定义消息ID生成
```java
@Override
protected <T> String generateMessageId(String topic, String key, T message) {
    // 自定义消息ID生成逻辑
    return customIdGenerator.generate();
}
```

4. 消息重试机制
```java
// 发送失败时会自动重试
messageRecord.setRetryCount(messageRecord.getRetryCount() + 1);
messageRecord.setNextRetryTime(LocalDateTime.now().plusMinutes(delayMinutes));
```

5. 消息监控
```java
// 可以通过消息记录表和消费记录表监控消息状态
List<MessageRecord> failedMessages = messageRecordService.findByStatus(MessageRecord.Status.SEND_FAILED);
List<MessageConsumed> consumedMessages = messageConsumedService.findByGroup(group);
```

### 4. 性能优化建议

1. 批量发送
- 适当增加 `batch-size` 和 `linger-ms`
- 使用异步发送方式

2. 消费者优化
- 合理设置 `max-poll-records`
- 根据业务需求选择是否启用自动提交
- 适当调整 `fetch-min-bytes` 和 `fetch-max-wait-ms`

3. 消息压缩
- 选择合适的压缩类型（snappy/gzip/lz4）
- 根据消息大小决定是否启用压缩

4. 分区策略
- 合理设置分区数
- 根据业务特点选择合适的分区策略

### 5. 监控指标

1. 生产者指标
- 发送成功率
- 发送延迟
- 重试次数
- 批量发送大小

2. 消费者指标
- 消费延迟
- 消费速率
- 重平衡次数
- 消息积压量

3. 系统指标
- CPU使用率
- 内存使用
- 网络IO
- 磁盘IO 