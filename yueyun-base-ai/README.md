# YueYun AI 增强模块

YueYun AI 增强模块是基于Spring框架开发的AI能力增强组件，通过简单的配置即可在您的应用中集成人工智能能力。目前支持阿里云DashScope服务，提供了简洁易用的API接口，帮助开发者快速实现AI应用。

## 特性

- 支持阿里云DashScope服务，对接通义系列大模型
- 提供统一的API接口，易于使用和扩展
- 支持文本对话、图片生成、文本向量等多种AI能力
- 自动配置和依赖注入，与Spring Boot无缝集成
- 支持流式响应和异步调用

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-ai</artifactId>
    <version>${yueyun.version}</version>
</dependency>
```

### 配置参数

在`application.yml`文件中添加以下配置：

```yaml
yueyun:
  ai:
    enabled: true
    # 默认AI提供商
    default-provider: dashscope
    # 阿里云DashScope配置
    dash-scope:
      api-key: 您的DashScope密钥  # 从阿里云获取，必填
      endpoint: https://dashscope.aliyuncs.com
      model:
        # 聊天模型，可选：qwen-turbo, qwen-plus, qwen-max, qwen-vl-plus
        chat: qwen-turbo
        # 图像生成模型，可选：wanx-v1, stable-diffusion-xl
        image-generation: wanx-v1
        # 嵌入模型
        embedding: text-embedding-v2
    # 请求配置
    request:
      # 系统提示词
      system-prompt: 你是一个智能助手，请根据用户提问提供准确、有用的信息。
      # 最大输出token数
      max-tokens: 2000
      # 温度参数(0.0-2.0)，数值越高回答越随机
      temperature: 0.7
      # top_p参数(0.0-1.0)
      top-p: 1.0
      # 结果格式：text或message
      result-format: message
```

### 启用AI功能

在SpringBoot启动类上添加`@EnableAI`注解：

```java
import online.yueyun.ai.annotation.EnableAI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAI
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 使用AI服务

通过依赖注入使用AIService：

```java
import online.yueyun.ai.model.ChatMessage;
import online.yueyun.ai.model.ChatResponse;
import online.yueyun.ai.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        // 简单聊天，直接返回内容
        return aiService.chat(message);
    }
    
    @GetMapping("/chat-full")
    public ChatResponse chatFull(@RequestParam String message) {
        // 完整聊天，返回详细信息
        ChatMessage userMessage = ChatMessage.user(message);
        return aiService.chat(Collections.singletonList(userMessage));
    }
    
    @GetMapping("/generate-image")
    public String generateImage(@RequestParam String prompt) {
        // 生成图片，返回图片URL
        return aiService.generateImage(prompt);
    }
}
```

### 流式响应

对于需要流式返回的场景，可以使用流式API：

```java
import online.yueyun.ai.model.ChatMessage;
import online.yueyun.ai.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class StreamAIController {
    
    @Autowired
    private AIService aiService;
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @GetMapping("/stream-chat")
    public SseEmitter streamChat(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter();
        
        executorService.execute(() -> {
            try {
                aiService.streamChat(message, content -> {
                    try {
                        emitter.send(content);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
}
```

## API参考

### AIService接口

主要方法：

- `String chat(String message)` - 简单聊天，发送单条消息并获取回复
- `String chat(String message, String model)` - 使用指定模型进行聊天
- `ChatResponse chat(ChatRequest request)` - 发送自定义请求获取完整响应
- `ChatResponse chat(List<ChatMessage> messages)` - 发送消息列表获取响应
- `void streamChat(String message, Consumer<String> consumer)` - 流式聊天
- `String generateImage(String prompt)` - 生成图像
- `List<Float> getEmbedding(String text)` - 获取文本嵌入向量

### 消息构建

```java
// 创建系统消息
ChatMessage systemMsg = ChatMessage.system("你是一个有用的AI助手");

// 创建用户消息
ChatMessage userMsg = ChatMessage.user("你好，请介绍一下自己");

// 创建助手消息
ChatMessage assistantMsg = ChatMessage.assistant("我是一个AI助手，有什么可以帮助你的？");

// 创建带图片的用户消息(多模态)
ChatMessage imageMsg = ChatMessage.userWithImages("这张图片是什么？", new String[]{"http://example.com/image.jpg"});

// 创建完整的聊天请求
ChatRequest request = ChatRequest.builder()
        .model("qwen-vl-plus")  // 使用视觉语言模型
        .temperature(0.5)
        .maxTokens(1000)
        .messages(Arrays.asList(systemMsg, userMsg, imageMsg))
        .build();
```

## 阿里云DashScope API密钥获取

要使用本模块，您需要获取阿里云DashScope服务的API密钥。请按照以下步骤操作：

1. 登录[阿里云官网](https://www.aliyun.com/)
2. 搜索"灵积模型服务"或直接访问[DashScope控制台](https://dashscope.console.aliyun.com/)
3. 按照提示注册并创建项目
4. 在API管理页面，创建并获取API密钥

## 注意事项

- DashScope服务是付费服务，请注意控制接口调用频率
- API密钥务必妥善保管，不要泄露给他人
- 请遵守阿里云的服务条款和相关法律法规
- 流式响应功能需注意资源释放，避免内存泄漏

## 扩展与定制

您可以通过实现AIService接口来扩展其他AI提供商的支持，通过以下方式注册到Spring容器中：

```java
@Bean
@ConditionalOnProperty(prefix = "yueyun.ai.your-provider", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(AIService.class)
public AIService yourAIService(AIProperties properties) {
    return new YourAIServiceImpl(properties);
}
```

## 版本历史

- 1.0.0 - 初始版本，支持阿里云DashScope服务 