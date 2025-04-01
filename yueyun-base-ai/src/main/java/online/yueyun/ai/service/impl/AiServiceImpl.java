package online.yueyun.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.config.AiProperties;
import online.yueyun.ai.model.AiRequest;
import online.yueyun.ai.model.AiResponse;
import online.yueyun.ai.service.AiService;
import online.yueyun.ai.service.extension.PromptEnhancer;
import online.yueyun.ai.service.extension.RagProcessor;
import online.yueyun.ai.template.AiTemplateManager;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
@Primary
@ConditionalOnMissingBean(name = "customAiService")
public class AiServiceImpl implements AiService {

    private final AiProperties properties;
    private final AiTemplateManager templateManager;
    
    /**
     * Spring AI聊天客户端
     */
    @Autowired
    private ChatClient chatClient;
    
    /**
     * Spring AI嵌入向量客户端
     */
    @Autowired(required = false)
    private EmbeddingClient embeddingClient;
    
    /**
     * 可选注入的RAG处理器
     * 可以实现该接口并注册为Bean来提供RAG功能
     */
    @Autowired(required = false)
    private List<RagProcessor> ragProcessors;
    
    /**
     * 可选注入的提示词增强器
     * 可以实现该接口并注册为Bean来提供提示词增强功能
     */
    @Autowired(required = false)
    private List<PromptEnhancer> promptEnhancers;

    public AiServiceImpl(AiProperties properties, AiTemplateManager templateManager) {
        this.properties = properties;
        this.templateManager = templateManager;
        log.info("初始化AI服务");
    }

    @Override
    public AiResponse chat(AiRequest request) {
        try {
            // 1. 参数校验
            if (request == null || !StringUtils.hasText(request.getPrompt())) {
                return AiResponse.error("请求参数无效");
            }

            // 2. 构建提示词
            PromptTemplate promptTemplate = new PromptTemplate(request.getPrompt());
            Prompt prompt = promptTemplate.create(request.getVariables());

            // 3. 调用AI服务
            ChatResponse response = chatClient.call(prompt);

            // 4. 处理响应
            if (response == null || response.getResult() == null) {
                return AiResponse.error("AI服务返回结果为空");
            }

            return AiResponse.success(response.getResult().getOutput().getContent());
        } catch (Exception e) {
            log.error("AI服务调用失败: {}", e.getMessage(), e);
            return AiResponse.error("AI服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public List<Double> getEmbedding(String text) {
        try {
            if (!StringUtils.hasText(text)) {
                throw new IllegalArgumentException("文本内容不能为空");
            }

            Document document = new Document(text);
            return embeddingClient.embed(document);
        } catch (Exception e) {
            log.error("获取文本向量失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文本向量失败", e);
        }
    }

    @Override
    public String generateText(String prompt) {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage("你是一个专业的文本生成助手，请根据提示词生成高质量的文本内容。"));
            messages.add(new UserMessage(prompt));

            ChatResponse response = chatClient.call(new Prompt(messages));
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("生成文本失败", e);
            throw new RuntimeException("生成文本失败", e);
        }
    }

    @Override
    public String generateTextWithTemplate(String template, Map<String, Object> variables) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Prompt prompt = promptTemplate.create(variables);
            
            ChatResponse response = chatClient.call(prompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("通过模板生成文本失败", e);
            throw new RuntimeException("通过模板生成文本失败", e);
        }
    }

    @Override
    public String summarize(String text) {
        try {
            Map<String, Object> variables = Map.of("text", text);
            return generateTextWithTemplate("请对以下文本进行摘要：{text}", variables);
        } catch (Exception e) {
            log.error("文本摘要生成失败", e);
            throw new RuntimeException("文本摘要生成失败", e);
        }
    }

    @Override
    public Map<String, Float> sentimentAnalysis(String text) {
        try {
            Map<String, Object> variables = Map.of("text", text);
            String result = generateTextWithTemplate("请分析以下文本的情感倾向，返回JSON格式结果：{text}", variables);
            
            // 解析结果
            Map<String, Float> sentiment = new HashMap<>();
            String[] parts = result.split(",");
            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue.length == 2) {
                    sentiment.put(keyValue[0].trim(), Float.parseFloat(keyValue[1].trim()));
                }
            }
            return sentiment;
        } catch (Exception e) {
            log.error("文本情感分析失败", e);
            throw new RuntimeException("文本情感分析失败", e);
        }
    }

    /**
     * 应用提示词增强
     * 
     * @param prompt 原始提示词
     * @return 增强后的提示词
     */
    private String enhancePrompt(String prompt) {
        if (CollectionUtils.isEmpty(promptEnhancers)) {
            return prompt;
        }
        
        String enhancedPrompt = prompt;
        for (PromptEnhancer enhancer : promptEnhancers) {
            enhancedPrompt = enhancer.enhance(enhancedPrompt);
        }
        
        return enhancedPrompt;
    }
    
    /**
     * 应用RAG处理器获取相关上下文
     * 
     * @param prompt 提示词
     * @return RAG检索的上下文
     */
    private String applyRagProcessors(String prompt) {
        if (CollectionUtils.isEmpty(ragProcessors)) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        for (RagProcessor processor : ragProcessors) {
            String result = processor.retrieveRelevantContext(prompt);
            if (StringUtils.hasText(result)) {
                if (context.length() > 0) {
                    context.append("\n\n");
                }
                context.append(result);
            }
        }
        
        return context.toString();
    }
    
    /**
     * 将RAG结果与提示词结合
     * 
     * @param prompt 原始提示词
     * @param ragContext RAG检索的上下文
     * @return 结合后的最终提示词
     */
    private String combinePromptWithContext(String prompt, String ragContext) {
        if (!StringUtils.hasText(ragContext)) {
            return prompt;
        }
        
        return String.format("基于以下上下文信息回答问题：\n\n%s\n\n问题：%s", ragContext, prompt);
    }

    /**
     * 生成模拟的嵌入向量（仅在没有EmbeddingClient时使用）
     */
    private List<Float> generateSimulatedEmbedding(String text) {
        List<Float> vector = new ArrayList<>();
        
        // 根据文本内容生成一个简单的伪向量（仅作演示）
        int vectorSize = 10; // 实际项目中可能是更大的维度，如1536
        for (int i = 0; i < vectorSize; i++) {
            // 使用文本特征影响向量
            float value = 0.1f + (float) (Math.sin(i + text.hashCode() % 10) * 0.5);
            // 确保值在[-1, 1]范围内
            vector.add(Math.max(-1, Math.min(1, value)));
        }
        
        return vector;
    }
} 