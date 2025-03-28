package online.yueyun.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI消息实体类
 * 
 * @author yueyun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {
    
    /**
     * 消息角色：用户、助手、系统等
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 创建用户消息
     */
    public static AiMessage userMessage(String content) {
        return AiMessage.builder()
                .role("user")
                .content(content)
                .build();
    }
    
    /**
     * 创建系统消息
     */
    public static AiMessage systemMessage(String content) {
        return AiMessage.builder()
                .role("system")
                .content(content)
                .build();
    }
    
    /**
     * 创建助手消息
     */
    public static AiMessage assistantMessage(String content) {
        return AiMessage.builder()
                .role("assistant")
                .content(content)
                .build();
    }
} 