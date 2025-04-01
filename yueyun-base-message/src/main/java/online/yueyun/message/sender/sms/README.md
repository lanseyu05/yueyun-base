# 短信服务提供商扩展指南

本模块实现了一个可扩展的短信服务发送框架，支持多种短信服务提供商。目前已实现阿里云短信服务，并提供了腾讯云短信的扩展模板。

## 架构说明

整体架构采用了策略模式，主要组件有：

1. `SmsProvider` 接口：定义了短信服务提供商的通用行为
2. `SmsProviderFactory`：管理和提供短信服务提供商实例
3. 具体实现类：如 `AliyunSmsProvider`、`TencentSmsProvider` 等

## 如何使用

在 `application.yml` 中配置短信服务：

```yaml
yueyun:
  message:
    sms:
      enabled: true
      provider: aliyun  # 可选值：aliyun, tencent, 或其他已实现的提供商
      access-key: YOUR_ACCESS_KEY
      secret-key: YOUR_SECRET_KEY
      region: cn-hangzhou
      sign-name: 您的签名
      default-template-code: SMS_123456789
```

在代码中使用短信发送功能：

```java
@Autowired
private MessageService messageService;

public void sendSms() {
    MessageRequest request = new MessageRequest();
    request.setChannel(MessageChannelEnum.SMS);
    request.setTemplateId("SMS_123456789");
    request.setReceivers(Collections.singletonList("13800138000"));
    request.setParams(Collections.singletonMap("code", "1234"));
    
    // 同步发送
    Long messageId = messageService.sendMessage(request);
    
    // 异步发送
    // Long messageId = messageService.sendMessageAsync(request);
}
```

## 如何扩展新的短信服务提供商

1. 创建一个新的类，实现 `SmsProvider` 接口：

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSmsProvider implements SmsProvider {
    private final SmsProperties smsProperties;
    
    @Override
    public String getName() {
        return "custom"; // 提供商标识，需唯一
    }
    
    @Override
    public boolean send(MessageRequest request) {
        // 实现发送短信的逻辑
        return true;
    }
    
    @Override
    public boolean initialize() {
        // 实现初始化逻辑
        return true;
    }
}
```

2. 在配置文件中使用新的提供商：

```yaml
yueyun:
  message:
    sms:
      provider: custom  # 使用自定义提供商
```

## 注意事项

1. 每个提供商实现类必须使用 `@Component` 注解，确保能被 Spring 自动扫描并注入
2. `getName()` 方法返回的字符串必须是唯一的，且与配置文件中的 `provider` 值保持一致（忽略大小写）
3. 在 `initialize()` 方法中进行必要的客户端初始化和配置验证
4. 在 `send()` 方法中处理异常并记录日志，确保不会抛出未捕获的异常 