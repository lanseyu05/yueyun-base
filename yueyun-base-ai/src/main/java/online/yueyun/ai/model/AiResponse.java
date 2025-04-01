package online.yueyun.ai.model;

import lombok.Data;

/**
 * AI响应对象
 */
@Data
public class AiResponse {
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 响应内容
     */
    private String content;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 成功响应
     */
    public static AiResponse success(String content) {
        AiResponse response = new AiResponse();
        response.setSuccess(true);
        response.setContent(content);
        return response;
    }
    
    /**
     * 错误响应
     */
    public static AiResponse error(String error) {
        AiResponse response = new AiResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
} 