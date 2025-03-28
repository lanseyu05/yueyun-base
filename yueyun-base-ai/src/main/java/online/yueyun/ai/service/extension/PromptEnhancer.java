package online.yueyun.ai.service.extension;

/**
 * 提示词增强器接口
 * 实现该接口可以对原始提示词进行增强处理
 * 例如：添加上下文信息、添加特定指令等
 * 
 * @author yueyun
 */
public interface PromptEnhancer {
    
    /**
     * 增强提示词
     * 
     * @param originalPrompt 原始提示词
     * @return 增强后的提示词
     */
    String enhance(String originalPrompt);
    
    /**
     * 获取增强器的优先级
     * 数字越小优先级越高，默认返回10
     * 多个增强器会按照优先级顺序依次处理
     * 
     * @return 优先级值
     */
    default int getOrder() {
        return 10;
    }
} 