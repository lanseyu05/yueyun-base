package online.yueyun.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI功能配置属性类
 * 
 * @author yueyun
 */
@Data
@ConfigurationProperties(prefix = "yueyun.ai")
public class AIProperties {

    /**
     * 是否启用AI功能
     */
    private boolean enabled = true;

    /**
     * 默认AI提供商
     */
    private String defaultProvider = "dashscope";

    /**
     * 阿里云DashScope配置
     */
    private DashScope dashScope = new DashScope();

    /**
     * 请求配置
     */
    private Request request = new Request();

    /**
     * 是否开启流式响应
     */
    private boolean streamResponse = false;

    /**
     * 缓存配置
     */
    private final Cache cache = new Cache();

    /**
     * 阿里云DashScope配置
     */
    @Data
    public static class DashScope {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 服务端点
         */
        private String endpoint = "https://dashscope.aliyuncs.com";

        /**
         * 模型配置
         */
        private Model model = new Model();

        /**
         * 代理主机
         */
        private String proxyHost;

        /**
         * 代理端口
         */
        private Integer proxyPort;

        /**
         * 连接超时时间（毫秒）
         */
        private long connectTimeout = 60000;

        /**
         * 响应超时时间（毫秒）
         */
        private long responseTimeout = 60000;

        /**
         * 模型配置
         */
        @Data
        public static class Model {
            /**
             * 聊天模型
             * 可选: qwen-turbo, qwen-plus, qwen-max, qwen-vl-plus
             */
            private String chat = "qwen-turbo";

            /**
             * 图像生成模型
             * 可选: wanx-v1, stable-diffusion-xl
             */
            private String imageGeneration = "wanx-v1";

            /**
             * 嵌入模型
             */
            private String embedding = "text-embedding-v2";
        }
    }

    /**
     * 请求配置
     */
    @Data
    public static class Request {
        /**
         * 系统提示词
         */
        private String systemPrompt = "你是一个智能助手，请根据用户提问提供准确、有用的信息。";

        /**
         * 最大输出token数
         */
        private Integer maxTokens = 2000;

        /**
         * 温度参数 (0.0-2.0)，数值越高回答越随机
         */
        private Double temperature = 0.7;

        /**
         * top_p参数 (0.0-1.0)
         */
        private Double topP = 1.0;
        
        /**
         * 结果格式：text或message
         */
        private String resultFormat = "message";
    }

    /**
     * 缓存配置
     */
    @Data
    public static class Cache {
        /**
         * 是否启用缓存
         */
        private boolean enabled = false;

        /**
         * 缓存过期时间（秒）
         */
        private long expireAfterWrite = 3600;

        /**
         * 最大缓存条目数
         */
        private long maximumSize = 1000;
    }
} 