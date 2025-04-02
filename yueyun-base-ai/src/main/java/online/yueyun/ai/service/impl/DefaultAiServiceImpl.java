package online.yueyun.ai.service.impl;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.model.AiRequest;
import online.yueyun.ai.model.AiResponse;
import online.yueyun.ai.service.AiService;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class DefaultAiServiceImpl implements AiService {

    @Value("${openai.api-key}")
    private String apiKey;

    private OpenAiService getOpenAiService() {
        return new OpenAiService(apiKey);
    }

    @Override
    public AiResponse chat(AiRequest request) {
        try {
            // 1. 参数校验
            if (request == null || !StringUtils.hasText(request.getPrompt())) {
                return AiResponse.error("请求参数无效");
            }

            // 2. 构建消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "你是一个专业的AI助手，请根据用户的问题提供准确的回答。"));
            messages.add(new ChatMessage("user", request.getPrompt()));

            // 3. 构建请求
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .build();

            // 4. 调用AI服务
            ChatCompletionResult result = getOpenAiService().createChatCompletion(completionRequest);

            // 5. 处理响应
            if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
                return AiResponse.error("AI服务返回结果为空");
            }

            return AiResponse.success(result.getChoices().get(0).getMessage().getContent());
        } catch (Exception e) {
            log.error("AI服务调用失败: {}", e.getMessage(), e);
            return AiResponse.error("AI服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public List<Double> getEmbedding(String text) {
        throw new UnsupportedOperationException("暂不支持获取文本向量");
    }

    @Override
    public String generateText(String prompt) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "你是一个专业的文本生成助手，请根据提示词生成高质量的文本内容。"));
            messages.add(new ChatMessage("user", prompt));

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .build();

            ChatCompletionResult result = getOpenAiService().createChatCompletion(completionRequest);
            return result.getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("生成文本失败", e);
            throw new RuntimeException("生成文本失败", e);
        }
    }

    @Override
    public String generateTextWithTemplate(String template, Map<String, Object> variables) {
        try {
            // 替换模板变量
            String prompt = template;
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                prompt = prompt.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }

            return generateText(prompt);
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