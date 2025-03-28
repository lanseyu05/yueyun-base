# 悦芸AI基础组件库

本模块基于Spring AI实现，提供了一套简单易用的AI功能接口，帮助开发者快速接入常用AI功能。

## 功能特性

- 文本生成：根据提示词生成文本内容
- 聊天对话：支持多轮对话交互
- 文本向量嵌入：将文本转换为向量形式，用于相似度计算等
- 文本摘要：自动生成长文本的摘要
- 情感分析：分析文本的情感倾向
- 提示词模板：支持通过模板和变量生成提示词
- 默认实现与自定义实现：支持扩展和替换默认实现
- RAG功能：支持检索增强生成，提高回答质量
- 提示词增强：支持多种提示词增强策略

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-ai</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 配置

在`application.properties`或`application.yml`中添加相关配置：

```properties
# AI功能配置
yueyun.ai.enabled=true
yueyun.ai.api-key=your_api_key_here
yueyun.ai.api-secret=your_api_secret_here

# 模型配置
yueyun.ai.model.default-model=qwen2
yueyun.ai.model.max-output-tokens=2048
yueyun.ai.model.temperature=0.7
yueyun.ai.model.top-p=0.9
```

### 使用示例

文本生成示例：

```java
@RestController
public class DemoController {
    
    @Autowired
    private AiService aiService;
    
    @GetMapping("/generate")
    public String generateText() {
        return aiService.generateText("写一首关于春天的诗");
    }
}
```

使用模板生成文本：

```java
@Component
public class TranslationService {
    
    @Autowired
    private AiService aiService;
    
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("text", text);
        variables.put("source_language", sourceLanguage);
        variables.put("target_language", targetLanguage);
        
        return aiService.generateTextWithTemplate("translation", variables);
    }
}
```

## 自定义实现

默认情况下，模块使用内置的默认实现。如果需要自定义AI服务实现，可以通过以下步骤：

### 1. 配置使用自定义服务

```properties
# 启用自定义服务实现
yueyun.ai.use-custom-service=true
```

### 2. 创建自定义实现类

```java
@Service
public class MyCustomAiService implements AiService {
    // 实现接口方法...
}
```

或者继承默认实现并覆盖部分方法：

```java
@Service
public class MyCustomAiService extends DefaultAiServiceImpl {
    
    @Override
    public String generateText(String prompt) {
        // 自定义实现...
    }
}
```

## RAG检索增强生成

本模块支持RAG（检索增强生成）功能，可以在生成回答前先检索相关知识，提高回答质量。

### 自定义RAG处理器

```java
@Component
public class MyRagProcessor implements RagProcessor {
    
    @Override
    public String retrieveRelevantContext(String query) {
        // 连接向量数据库或其他知识库检索相关信息
        // ...
        return "检索到的知识内容";
    }
    
    @Override
    public int getOrder() {
        // 设置优先级，数字越小优先级越高
        return 10;
    }
}
```

### RAG处理流程

1. 接收用户提问
2. 通过RagProcessor检索相关知识
3. 将检索到的知识与用户提问结合
4. 使用大语言模型生成回答

## 提示词增强

本模块支持多种提示词增强策略，可以提高模型回答质量和符合特定领域需求。

### 自定义提示词增强器

```java
@Component
public class MyPromptEnhancer implements PromptEnhancer {
    
    @Override
    public String enhance(String originalPrompt) {
        // 增强提示词
        return "增强的前缀 " + originalPrompt;
    }
    
    @Override
    public int getOrder() {
        // 设置优先级，数字越小优先级越高
        return 10;
    }
}
```

### 提示词增强流程

1. 接收原始提示词
2. 按优先级顺序应用各个PromptEnhancer
3. 得到增强后的提示词
4. 使用增强后的提示词生成回答

## 更多用法

更详细的使用方法请参考代码示例和接口文档。 