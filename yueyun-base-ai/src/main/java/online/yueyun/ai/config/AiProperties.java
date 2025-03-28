package online.yueyun.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI模块配置属性
 * 
 * @author yueyun
 */
@Data
@ConfigurationProperties(prefix = "yueyun.ai")
public class AiProperties {
    
    /**
     * 是否启用AI功能
     */
    private boolean enabled = true;
    
    /**
     * 是否使用自定义服务实现
     * 设置为true时，将启用自定义的AiService实现
     */
    private boolean useCustomService = false;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * API秘钥密码
     */
    private String apiSecret;
    
    /**
     * 模型配置
     */
    private ModelConfig model = new ModelConfig();
    
    /**
     * 模型配置类
     */
    @Data
    public static class ModelConfig {
        /**
         * 默认模型名称
         */
        private String defaultModel = "qwen2";
        
        /**
         * 最大输出令牌数
         */
        private Integer maxOutputTokens = 2048;
        
        /**
         * 温度参数，控制生成文本的随机性
         */
        private Float temperature = 0.7f;
        
        /**
         * 输出随机性参数，和temperature类似但更加平滑
         */
        private Float topP = 0.9f;
    }
} 