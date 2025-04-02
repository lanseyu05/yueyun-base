# YueYun Base MQ 模块

## 简介
`yueyun-base-mq` 是消息队列集成模块，支持Kafka、RocketMQ和RabbitMQ，提供了统一的消息发送和消费接口。

## 主要功能

### 1. 消息发送
- 同步发送
- 异步发送
- 批量发送
- 延迟消息
- 事务消息

### 2. 消息消费
- 消息监听器
- 消息重试机制
- 死信队列处理
- 消息过滤
- 消息追踪

### 3. 消息管理
- 消息监控
- 消息统计
- 消息追踪
- 消息补偿

### 4. 配置管理
- 多环境配置
- 动态配置
- 配置热更新

## 使用示例

### 消息发送
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final MessageProducer messageProducer;
    
    public void createOrder(Order order) {
        // 同步发送
        messageProducer.syncSend("order-topic", order);
        
        // 异步发送
        messageProducer.asyncSend("order-topic", order);
        
        // 延迟发送
        messageProducer.delaySend("order-topic", order, 30, TimeUnit.MINUTES);
    }
}
```

### 消息消费
```java
@Component
@RequiredArgsConstructor
public class OrderConsumer {
    
    @MessageListener(topic = "order-topic", group = "order-group")
    public void consume(Message<Order> message) {
        Order order = message.getPayload();
        // 处理订单
        orderService.process(order);
    }
    
    @Retryable(
        value = {BusinessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public void processWithRetry(Order order) {
        // 处理订单，失败时自动重试
    }
}
```

### 事务消息
```java
@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionMessageProducer messageProducer;
    
    @Transactional
    public void createOrder(Order order) {
        // 保存订单
        orderMapper.insert(order);
        
        // 发送事务消息
        messageProducer.sendTransactionMessage(
            "order-topic",
            order,
            order.getId()
        );
    }
}
```

## 配置说明

### Kafka配置
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: yueyun-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
```

### RocketMQ配置
```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: yueyun-producer-group
    send-message-timeout: 3000
  consumer:
    group: yueyun-consumer-group
```

### RabbitMQ配置
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- Kafka Client 3.6.0+
- RocketMQ Client 4.9.7+
- RabbitMQ Client 5.20.0+ 