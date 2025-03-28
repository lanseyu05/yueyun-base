package online.yueyun.ai.service;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 * 
 * @author yueyun
 */
public interface AiService {

    /**
     * 生成文本
     *
     * @param prompt 提示词
     * @return 生成的文本
     */
    String generateText(String prompt);

    /**
     * 通过模板生成文本
     *
     * @param template 模板名称
     * @param variables 变量参数
     * @return 生成的文本
     */
    String generateTextWithTemplate(String template, Map<String, Object> variables);

    /**
     * 聊天对话
     *
     * @param messages 消息列表
     * @return 助手回复
     */
    String chat(List<Map<String, String>> messages);

    /**
     * 文本内容向量嵌入
     * 
     * @param text 输入文本
     * @return 向量嵌入结果
     */
    List<Float> embedding(String text);
    
    /**
     * 文本摘要生成
     * 
     * @param text 待摘要的文本
     * @return 摘要结果
     */
    String summarize(String text);
    
    /**
     * 文本情感分析
     * 
     * @param text 待分析文本
     * @return 情感分析结果
     */
    Map<String, Float> sentimentAnalysis(String text);
} 