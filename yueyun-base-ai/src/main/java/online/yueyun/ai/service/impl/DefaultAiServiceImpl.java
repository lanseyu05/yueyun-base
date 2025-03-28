package online.yueyun.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.config.AiProperties;
import online.yueyun.ai.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认AI服务实现类，提供基础功能实现
 * 使用@Primary和@ConditionalOnMissingBean注解，
 * 允许用户通过自定义实现类来覆盖默认行为
 * 
 * @author yueyun
 */
@Slf4j
@Service
@Primary
@ConditionalOnMissingBean(name = "customAiService")
public class DefaultAiServiceImpl implements AiService {

    @Autowired
    private AiProperties aiProperties;

    @Override
    public String generateText(String prompt) {
        log.info("默认实现 - 生成文本，提示词: {}", prompt);
        return "默认实现的文本生成功能";
    }

    @Override
    public String generateTextWithTemplate(String template, Map<String, Object> variables) {
        log.info("默认实现 - 使用模板生成文本，模板: {}, 变量: {}", template, variables);
        return "默认实现的模板文本生成功能";
    }

    @Override
    public String chat(List<Map<String, String>> messages) {
        log.info("默认实现 - 聊天对话，消息数量: {}", messages.size());
        return "默认实现的聊天对话功能";
    }

    @Override
    public List<Float> embedding(String text) {
        log.info("默认实现 - 生成文本嵌入向量，文本长度: {}", text.length());
        return List.of(0.1f, 0.2f, 0.3f, 0.4f);
    }
    
    @Override
    public String summarize(String text) {
        log.info("默认实现 - 生成文本摘要，文本长度: {}", text.length());
        return "默认实现的文本摘要功能";
    }
    
    @Override
    public Map<String, Float> sentimentAnalysis(String text) {
        log.info("默认实现 - 进行情感分析，文本长度: {}", text.length());
        Map<String, Float> result = new HashMap<>();
        result.put("积极", 0.5f);
        result.put("消极", 0.3f);
        result.put("中性", 0.2f);
        return result;
    }
} 