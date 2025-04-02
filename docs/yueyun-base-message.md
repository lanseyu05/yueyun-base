# YueYun Base Message 模块

## 简介
`yueyun-base-message` 是消息通知模块，支持短信、邮件、站内信、WebSocket等多种通知方式，提供了统一的消息发送接口。

## 主要功能

### 1. 短信通知
- 阿里云短信
- 腾讯云短信
- 华为云短信
- 自定义短信服务商

### 2. 邮件通知
- 简单邮件
- HTML邮件
- 模板邮件
- 附件邮件
- 群发邮件

### 3. 站内信
- 系统通知
- 业务通知
- 消息模板
- 消息状态
- 消息推送

### 4. WebSocket
- 实时消息推送
- 消息订阅
- 消息广播
- 消息确认
- 消息重试

## 使用示例

### 消息配置
```java
@Configuration
@RequiredArgsConstructor
public class MessageConfig {
    
    @Value("${message.sms.provider}")
    private String smsProvider;
    
    @Value("${message.sms.access-key}")
    private String smsAccessKey;
    
    @Value("${message.sms.secret-key}")
    private String smsSecretKey;
    
    @Value("${message.sms.sign-name}")
    private String smsSignName;
    
    @Value("${message.sms.template-code}")
    private String smsTemplateCode;
    
    @Bean
    public MessageService messageService() {
        return new MessageService(
            createSmsService(),
            createEmailService(),
            createWebSocketService()
        );
    }
    
    private SmsService createSmsService() {
        switch (smsProvider) {
            case "aliyun":
                return new AliyunSmsService(smsAccessKey, smsSecretKey, smsSignName, smsTemplateCode);
            case "tencent":
                return new TencentSmsService();
            case "huawei":
                return new HuaweiSmsService();
            default:
                throw new IllegalArgumentException("Unsupported SMS provider: " + smsProvider);
        }
    }
}
```

### 消息发送
```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final MessageService messageService;
    
    public void sendVerificationCode(String phone, String code) {
        // 发送短信验证码
        messageService.sendSms(
            phone,
            "verification",
            Map.of("code", code)
        );
    }
    
    public void sendEmail(String to, String subject, String content) {
        // 发送邮件
        messageService.sendEmail(
            to,
            subject,
            content,
            true
        );
    }
    
    public void sendSystemNotification(Long userId, String title, String content) {
        // 发送系统通知
        messageService.sendNotification(
            userId,
            title,
            content,
            NotificationType.SYSTEM
        );
    }
}
```

### WebSocket消息
```java
@RestController
@RequiredArgsConstructor
public class WebSocketController {
    
    private final WebSocketService webSocketService;
    
    @MessageMapping("/chat")
    public void handleChat(ChatMessage message) {
        // 处理聊天消息
        webSocketService.sendMessage(
            message.getToUserId(),
            message.getContent()
        );
    }
    
    @MessageMapping("/broadcast")
    public void handleBroadcast(BroadcastMessage message) {
        // 处理广播消息
        webSocketService.broadcastMessage(
            message.getContent()
        );
    }
}
```

## 配置说明

### 基础配置
```yaml
message:
  # 是否启用消息服务
  enabled: true
  # 消息重试次数
  retry-count: 3
  # 消息重试间隔（秒）
  retry-interval: 30
  # 消息过期时间（天）
  expire-days: 7
```

### 短信配置
```yaml
message:
  sms:
    provider: aliyun
    access-key: your-access-key
    secret-key: your-secret-key
    sign-name: your-sign-name
    template-code: your-template-code
```

### 邮件配置
```yaml
message:
  email:
    host: smtp.example.com
    port: 465
    username: your-username
    password: your-password
    from: noreply@example.com
    ssl: true
```

### WebSocket配置
```yaml
message:
  websocket:
    endpoint: /ws
    allowed-origins: "*"
    heartbeat-interval: 30
    max-text-message-size: 8192
    max-binary-message-size: 8192
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- Spring WebSocket 6.1.0+
- JavaMail 1.6.2+
- 阿里云短信SDK 2.0.23+
- 腾讯云短信SDK 3.0.1+
- 华为云短信SDK 3.0.8+ 