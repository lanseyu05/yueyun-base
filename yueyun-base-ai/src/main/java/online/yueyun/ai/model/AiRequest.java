package online.yueyun.ai.model;

import lombok.Data;
import java.util.Map;

/**
 * AI请求对象
 */
@Data
public class AiRequest {
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 变量映射
     */
    private Map<String, Object> variables;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * 最大token数
     */
    private Integer maxTokens;
} 