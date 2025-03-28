package online.yueyun.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.config.AiProperties;
import online.yueyun.ai.service.AiService;
import online.yueyun.ai.service.extension.PromptEnhancer;
import online.yueyun.ai.service.extension.RagProcessor;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI服务自定义实现类
 * 需要客户端显式启用才会生效
 * 用户需要在自己的配置中声明一个名为"customAiService"的空Bean来触发此实现
 * 
 * @author yueyun
 */
@Slf4j
@Service("customAiService")
@ConditionalOnBean(name = "enableCustomAiService")
public class AiServiceImpl implements AiService {

    @Autowired
    private AiProperties aiProperties;
    
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

    @Override
    public String generateText(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "提示词不能为空";
        }
        
        log.info("自定义实现 - 生成文本，提示词: {}", prompt);
        
        // 1. 应用提示词增强
        String enhancedPrompt = enhancePrompt(prompt);
        
        // 2. 应用RAG处理，获取相关知识
        String ragContext = applyRagProcessors(enhancedPrompt);
        
        // 3. 将RAG结果与提示词结合
        String finalPrompt = combineRagAndPrompt(enhancedPrompt, ragContext);
        
        log.info("应用RAG和提示词增强后，最终提示词: {}", finalPrompt);
        
        // 4. 实际调用模型API生成响应
        try {
            // 实际调用Spring AI的ChatClient生成回复
            List<Message> messages = new ArrayList<>();
            
            // 如果有RAG结果，添加为系统消息
            if (StringUtils.hasText(ragContext)) {
                messages.add(new SystemMessage("请基于以下信息回答用户问题: \n\n" + ragContext));
            }
            
            // 添加用户提问
            messages.add(new UserMessage(finalPrompt));
            
            // 创建提示
            Prompt aiPrompt = new Prompt(messages);
            
            // 调用AI模型获取回复
            ChatResponse response = chatClient.call(aiPrompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("生成文本出错", e);
            return "生成文本时遇到错误：" + e.getMessage();
        }
    }

    @Override
    public String generateTextWithTemplate(String template, Map<String, Object> variables) {
        if (template == null || template.trim().isEmpty()) {
            return "模板名称不能为空";
        }
        
        if (variables == null) {
            variables = new HashMap<>();
        }
        
        log.info("自定义实现 - 使用模板生成文本，模板: {}, 变量: {}", template, variables);
        
        // 应用提示词增强和变量处理
        Map<String, Object> enhancedVariables = enhanceTemplateVariables(variables);
        
        try {
            // 使用Spring AI的PromptTemplate处理模板
            PromptTemplate promptTemplate = new PromptTemplate(template);
            String renderedPrompt = promptTemplate.render(enhancedVariables);
            
            // 使用渲染后的模板生成文本
            return generateText(renderedPrompt);
        } catch (Exception e) {
            log.error("使用模板生成文本出错", e);
            return "使用模板生成文本时遇到错误：" + e.getMessage();
        }
    }

    @Override
    public String chat(List<Map<String, String>> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return "消息列表不能为空";
        }
        
        log.info("自定义实现 - 聊天对话，消息数量: {}", messages.size());
        
        try {
            // 提取最后一个用户消息用于RAG
            Optional<String> lastUserMessage = extractLastUserMessage(messages);
            
            // 转换消息列表为Spring AI消息格式
            List<Message> aiMessages = new ArrayList<>();
            String ragContext = "";
            
            // 如果有用户消息，进行RAG处理
            if (lastUserMessage.isPresent()) {
                ragContext = applyRagProcessors(lastUserMessage.get());
            }
            
            // 如果有RAG结果，添加为系统消息
            if (StringUtils.hasText(ragContext)) {
                aiMessages.add(new SystemMessage("请基于以下信息回答用户问题: \n\n" + ragContext));
            }
            
            // 添加对话历史消息
            for (Map<String, String> message : messages) {
                String role = message.getOrDefault("role", "");
                String content = message.getOrDefault("content", "");
                
                if (!StringUtils.hasText(content)) {
                    continue;
                }
                
                switch (role) {
                    case "user":
                        aiMessages.add(new UserMessage(content));
                        break;
                    case "assistant":
                        // Spring AI 目前可能不直接支持AssistantMessage，根据实际情况调整
                        // 这里可能需要将助手消息转换为用户消息对
                        if (aiMessages.size() > 0 && aiMessages.get(aiMessages.size() - 1) instanceof UserMessage) {
                            // 如果前一条是用户消息，保留助手回复，模拟对话历史
                            log.debug("保留助手回复历史: {}", content);
                            // 在实际场景中可能需要特殊处理或转换
                        }
                        break;
                    case "system":
                        // 系统消息应该放在最前面
                        if (aiMessages.isEmpty() || !(aiMessages.get(0) instanceof SystemMessage)) {
                            aiMessages.add(0, new SystemMessage(content));
                        }
                        break;
                    default:
                        // 忽略未知角色消息
                        log.warn("忽略未知角色消息: {}", role);
                }
            }
            
            // 创建提示
            Prompt aiPrompt = new Prompt(aiMessages);
            
            // 调用AI模型获取回复
            ChatResponse response = chatClient.call(aiPrompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("聊天对话生成出错", e);
            return "生成对话回复时遇到错误：" + e.getMessage();
        }
    }

    @Override
    public List<Float> embedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        
        log.info("自定义实现 - 生成文本嵌入向量，文本长度: {}", text.length());
        
        try {
            // 检查是否有嵌入客户端
            if (embeddingClient == null) {
                log.warn("嵌入向量客户端未注入，使用模拟向量");
                return generateSimulatedEmbedding(text);
            }
            
            // 调用嵌入客户端获取向量
            List<Double> embeddingResult = embeddingClient.embed(text);
            
            // 转换Double列表为Float列表
            return embeddingResult.stream()
                .map(Double::floatValue)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("生成嵌入向量出错", e);
            return List.of();
        }
    }
    
    @Override
    public String summarize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "待摘要文本不能为空";
        }
        
        log.info("自定义实现 - 生成文本摘要，文本长度: {}", text.length());
        
        try {
            // 创建系统提示和用户提示
            Message systemMessage = new SystemMessage("你是一个文本摘要专家，请生成简明扼要的摘要，保留原文的主要信息，去除冗余内容。");
            Message userMessage = new UserMessage("请为以下文本生成摘要：\n\n" + text);
            
            // 创建提示
            Prompt aiPrompt = new Prompt(List.of(systemMessage, userMessage));
            
            // 调用AI模型获取摘要
            ChatResponse response = chatClient.call(aiPrompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("生成文本摘要出错", e);
            return "生成摘要时遇到错误：" + e.getMessage();
        }
    }
    
    @Override
    public Map<String, Float> sentimentAnalysis(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Map.of("错误", 1.0f);
        }
        
        log.info("自定义实现 - 进行情感分析，文本长度: {}", text.length());
        
        try {
            // 创建系统提示和用户提示
            Message systemMessage = new SystemMessage(
                "你是一个情感分析专家，请分析输入文本的情感倾向，并以JSON格式返回分析结果。" +
                "结果必须包含三个键值对：\"积极\": [0-1之间的浮点数], \"消极\": [0-1之间的浮点数], \"中性\": [0-1之间的浮点数]。" +
                "三个值的总和应等于1。仅返回JSON，不要添加解释。"
            );
            Message userMessage = new UserMessage("请分析以下文本的情感倾向：\n\n" + text);
            
            // 创建提示
            Prompt aiPrompt = new Prompt(List.of(systemMessage, userMessage));
            
            // 调用AI模型获取分析结果
            ChatResponse response = chatClient.call(aiPrompt);
            String jsonResult = response.getResult().getOutput().getContent();
            
            // 解析JSON结果
            try {
                // 简单解析，实际生产中应使用Jackson或Gson等库
                String cleaned = jsonResult.replaceAll("```json", "").replaceAll("```", "").trim();
                
                Map<String, Float> result = new HashMap<>();
                
                // 提取积极分数
                String positivePattern = "\"积极\"\\s*:\\s*([0-9]*\\.?[0-9]+)";
                java.util.regex.Matcher positiveMatcher = java.util.regex.Pattern.compile(positivePattern).matcher(cleaned);
                if (positiveMatcher.find()) {
                    result.put("积极", Float.parseFloat(positiveMatcher.group(1)));
                } else {
                    result.put("积极", 0.33f);
                }
                
                // 提取消极分数
                String negativePattern = "\"消极\"\\s*:\\s*([0-9]*\\.?[0-9]+)";
                java.util.regex.Matcher negativeMatcher = java.util.regex.Pattern.compile(negativePattern).matcher(cleaned);
                if (negativeMatcher.find()) {
                    result.put("消极", Float.parseFloat(negativeMatcher.group(1)));
                } else {
                    result.put("消极", 0.33f);
                }
                
                // 提取中性分数
                String neutralPattern = "\"中性\"\\s*:\\s*([0-9]*\\.?[0-9]+)";
                java.util.regex.Matcher neutralMatcher = java.util.regex.Pattern.compile(neutralPattern).matcher(cleaned);
                if (neutralMatcher.find()) {
                    result.put("中性", Float.parseFloat(neutralMatcher.group(1)));
                } else {
                    result.put("中性", 0.34f);
                }
                
                return result;
            } catch (Exception e) {
                log.error("解析情感分析结果出错", e);
                // 返回默认值
                Map<String, Float> defaultResult = new HashMap<>();
                defaultResult.put("积极", 0.33f);
                defaultResult.put("消极", 0.33f);
                defaultResult.put("中性", 0.34f);
                return defaultResult;
            }
        } catch (Exception e) {
            log.error("情感分析出错", e);
            return Map.of("错误", 1.0f);
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
    private String combineRagAndPrompt(String prompt, String ragContext) {
        if (!StringUtils.hasText(ragContext)) {
            return prompt;
        }
        
        return "基于以下信息：\n\n" + ragContext + "\n\n回答问题：" + prompt;
    }
    
    /**
     * 增强模板变量
     * 
     * @param variables 原始变量
     * @return 增强后的变量
     */
    private Map<String, Object> enhanceTemplateVariables(Map<String, Object> variables) {
        // 可以在这里对模板变量进行处理和增强
        // 例如对某些变量进行RAG处理等
        Map<String, Object> enhancedVars = new HashMap<>(variables);
        
        // 处理文本类变量
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (entry.getValue() instanceof String && ((String) entry.getValue()).length() > 50) {
                // 对长文本变量应用RAG处理
                String text = (String) entry.getValue();
                String context = applyRagProcessors(text);
                
                if (StringUtils.hasText(context)) {
                    // 将RAG结果添加为新变量
                    enhancedVars.put(entry.getKey() + "_context", context);
                }
            }
        }
        
        return enhancedVars;
    }
    
    /**
     * 从消息列表中提取最后一个用户消息
     * 
     * @param messages 消息列表
     * @return 最后一个用户消息，如果没有则返回空
     */
    private Optional<String> extractLastUserMessage(List<Map<String, String>> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Optional.empty();
        }
        
        // 倒序查找最近的用户消息
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, String> message = messages.get(i);
            if ("user".equals(message.get("role"))) {
                return Optional.ofNullable(message.get("content"));
            }
        }
        
        return Optional.empty();
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