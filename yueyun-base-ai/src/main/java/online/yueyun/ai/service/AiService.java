package online.yueyun.ai.service;

import online.yueyun.ai.model.AiRequest;
import online.yueyun.ai.model.AiResponse;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 * 
 * @author yueyun
 */
public interface AiService {

    /**
     * 聊天对话
     */
    AiResponse chat(AiRequest request);

    /**
     * 获取文本向量
     */
    List<Double> getEmbedding(String text);

    /**
     * 生成文本
     */
    String generateText(String prompt);

    /**
     * 使用模板生成文本
     */
    String generateTextWithTemplate(String template, Map<String, Object> variables);

    /**
     * 文本摘要
     */
    String summarize(String text);

    /**
     * 情感分析
     */
    Map<String, Float> sentimentAnalysis(String text);
} 