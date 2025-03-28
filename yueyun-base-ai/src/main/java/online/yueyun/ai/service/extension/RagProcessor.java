package online.yueyun.ai.service.extension;

import java.util.Map;

/**
 * RAG (检索增强生成) 处理器接口
 * 实现该接口可以提供检索相关信息的能力
 * 例如：从向量数据库、知识库等检索相关上下文
 * 
 * @author yueyun
 */
public interface RagProcessor {
    
    /**
     * 检索与提示词相关的上下文信息
     * 
     * @param query 查询内容
     * @return 相关上下文信息，如果没有则返回空字符串
     */
    String retrieveRelevantContext(String query);
    
    /**
     * 检索与提示词相关的上下文信息，支持额外参数
     * 
     * @param query 查询内容
     * @param options 检索选项，例如相似度阈值、检索数量等
     * @return 相关上下文信息，如果没有则返回空字符串
     */
    default String retrieveRelevantContext(String query, Map<String, Object> options) {
        return retrieveRelevantContext(query);
    }
    
    /**
     * 获取处理器的优先级
     * 数字越小优先级越高，默认返回10
     * 多个处理器会按照优先级顺序依次处理
     * 
     * @return 优先级值
     */
    default int getOrder() {
        return 10;
    }
    
    /**
     * 获取处理器名称
     * 
     * @return 处理器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
} 