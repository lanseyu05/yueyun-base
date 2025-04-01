package online.yueyun.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.model.AiRequest;
import online.yueyun.ai.model.AiResponse;
import online.yueyun.ai.service.AiService;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认AI服务实现类
 */
@Slf4j
@Service
public class DefaultAiServiceImpl implements AiService {

    @Autowired
    private ChatClient chatClient;

    @Autowired(required = false)
    private EmbeddingClient embeddingClient;

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
} 