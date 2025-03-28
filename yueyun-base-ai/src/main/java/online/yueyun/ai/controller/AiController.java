package online.yueyun.ai.controller;

import online.yueyun.ai.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI功能演示控制器
 * 
 * @author yueyun
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 生成文本
     */
    @PostMapping("/generate")
    public String generateText(@RequestBody Map<String, String> request) {
        return aiService.generateText(request.get("prompt"));
    }

    /**
     * 聊天对话
     */
    @PostMapping("/chat")
    public String chat(@RequestBody List<Map<String, String>> messages) {
        return aiService.chat(messages);
    }

    /**
     * 生成文本向量嵌入
     */
    @PostMapping("/embedding")
    public List<Float> embedding(@RequestBody Map<String, String> request) {
        return aiService.embedding(request.get("text"));
    }
    
    /**
     * 文本摘要
     */
    @PostMapping("/summarize")
    public String summarize(@RequestBody Map<String, String> request) {
        return aiService.summarize(request.get("text"));
    }
    
    /**
     * 情感分析
     */
    @PostMapping("/sentiment")
    public Map<String, Float> sentiment(@RequestBody Map<String, String> request) {
        return aiService.sentimentAnalysis(request.get("text"));
    }
} 