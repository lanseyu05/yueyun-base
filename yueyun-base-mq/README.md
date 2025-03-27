# 消息队列模块 (yueyun-base-mq)

消息队列模块是YueYun基础框架的一部分，提供了统一的消息队列抽象接口，并支持多种消息队列实现，包括Kafka、RocketMQ和RabbitMQ。

## 特性

- 提供统一的消息服务接口，支持多种消息队列实现
- 支持同步和异步发送消息
- 支持消息订阅功能
- 支持消息头部信息设置
- 自动配置和条件装配
- 通过注解方式快速启用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-mq</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用消息队列功能

在应用的启动类上添加`@EnableMq`注解：

```java
@SpringBootApplication
@EnableMq
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置消息队列属性

在`application.yml`或`application.properties`中配置消息队列属性：

```yaml
yueyun:
  mq:
    type: kafka  # 可选：kafka, rocketmq, rabbitmq
    enabled: true
    default-topic: yueyun-topic
    default-group: yueyun-group
    
    # Kafka配置（当type=kafka时生效）
    kafka:
      enabled: true
      bootstrap-servers: localhost:9092
      producer:
        retries: 3
        batch-size: 16384
        buffer-memory: 33554432
      consumer:
        enable-auto-commit: true
        auto-commit-interval-ms: 1000
        session-timeout-ms: 30000
    
    # RocketMQ配置（当type=rocketmq时生效）
    rocket-mq:
      enabled: false
      name-server: localhost:9876
      producer-group: yueyun-producer-group
      consumer-group: yueyun-consumer-group
      send-message-timeout: 3000
      compress-msg-body-over-howmuch: 4096
    
    # RabbitMQ配置（当type=rabbitmq时生效）
    rabbit-mq:
      enabled: false
      host: localhost
      port: 5672
      username: guest
      password: guest
      virtual-host: /
```

### 4. 使用消息服务接口

```java
@Service
public class YourService {
    
    private final MessageService messageService;
    
    @Autowired
    public YourService(MessageService messageService) {
        this.messageService = messageService;
    }
    
    public void sendMessage(String topic, String key, Object message) throws Exception {
        // 同步发送消息
        messageService.send(topic, key, message);
        
        // 异步发送消息
        messageService.sendAsync(topic, key, message)
            .thenAccept(v -> System.out.println("消息发送成功"))
            .exceptionally(ex -> {
                System.err.println("消息发送失败: " + ex.getMessage());
                return null;
            });
    }
    
    public void subscribeMessages(String topic, String group) {
        // 订阅消息
        messageService.subscribe(topic, group, String.class, message -> {
            System.out.println("收到消息: " + message);
        });
    }
}
```

## 模块架构

```
yueyun-base-mq
├── config             - 配置类
│   ├── MqProperties   - 配置属性类
│   └── MqAutoConfiguration - 自动配置类
├── service            - 服务接口
│   └── MessageService - 消息服务接口
├── kafka              - Kafka实现
│   ├── KafkaConfiguration - Kafka配置类
│   └── KafkaMessageServiceImpl - Kafka消息服务实现
├── rocketmq           - RocketMQ实现
│   ├── RocketMqConfiguration - RocketMQ配置类
│   └── RocketMqMessageServiceImpl - RocketMQ消息服务实现
└── rabbitmq           - RabbitMQ实现
    ├── RabbitMqConfiguration - RabbitMQ配置类
    └── RabbitMqMessageServiceImpl - RabbitMQ消息服务实现
```

## 高级用法

### 1. 使用消息头

```java
Map<String, Object> headers = new HashMap<>();
headers.put("header1", "value1");
headers.put("timestamp", System.currentTimeMillis());

messageService.send("topic", "key", message, headers);
```

### 2. 自定义消费者

```java
// 使用Lambda表达式作为消费者
messageService.subscribe("topic", "group", MyEvent.class, event -> {
    System.out.println("处理事件: " + event);
    // 处理消息逻辑...
});
```

### 3. 使用Kafka监听器

```java
@Component
public class KafkaConsumer {

    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void listen(String message) {
        System.out.println("收到Kafka消息: " + message);
    }
}
```

## 注意事项

1. 不同消息队列实现的特性和行为可能有所不同
2. RocketMQ和RabbitMQ默认是禁用的，需要通过配置启用
3. 确保消息队列服务器已经正确安装和配置
4. 异步发送消息时，应正确处理异常情况 