package online.yueyun.ai.controller;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.model.AiRequest;
import online.yueyun.ai.model.AiResponse;
import online.yueyun.ai.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public AiResponse chat(@RequestBody AiRequest request) {
        return aiService.chat(request);
    }

    @PostMapping("/embedding")
    public List<Double> getEmbedding(@RequestBody Map<String, String> request) {
        return aiService.getEmbedding(request.get("text"));
    }

    @PostMapping("/generate")
    public String generateText(@RequestBody Map<String, String> request) {
        return aiService.generateText(request.get("prompt"));
    }

    @PostMapping("/generate/template")
    public String generateTextWithTemplate(@RequestBody Map<String, Object> request) {
        return aiService.generateTextWithTemplate(
            (String) request.get("template"),
            (Map<String, Object>) request.get("variables")
        );
    }

    @PostMapping("/summarize")
    public String summarize(@RequestBody Map<String, String> request) {
        return aiService.summarize(request.get("text"));
    }

    @PostMapping("/sentiment")
    public Map<String, Float> sentimentAnalysis(@RequestBody Map<String, String> request) {
        return aiService.sentimentAnalysis(request.get("text"));
    }
} 