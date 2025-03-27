package online.yueyun.ai.service;

import online.yueyun.ai.model.ChatMessage;
import online.yueyun.ai.model.ChatRequest;
import online.yueyun.ai.model.ChatResponse;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI服务接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface AIService {

    /**
     * 简单聊天，发送单条消息并获取回复
     *
     * @param message 消息内容
     * @return 回复内容
     */
    String chat(String message);

    /**
     * 使用指定模型进行聊天
     *
     * @param message 消息内容
     * @param model 模型名称
     * @return 回复内容
     */
    String chat(String message, String model);

    /**
     * 发送自定义请求获取完整响应
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 发送消息列表获取响应
     *
     * @param messages 消息列表
     * @return 聊天响应
     */
    ChatResponse chat(List<ChatMessage> messages);

    /**
     * 使用指定参数发送消息列表
     *
     * @param messages 消息列表
     * @param temperature 温度参数
     * @param model 模型名称
     * @return 聊天响应
     */
    ChatResponse chat(List<ChatMessage> messages, Double temperature, String model);

    /**
     * 流式聊天，发送单条消息并通过回调获取回复
     *
     * @param message 消息内容
     * @param consumer 回复内容消费者
     */
    void streamChat(String message, Consumer<String> consumer);

    /**
     * 流式聊天，发送自定义请求并通过回调获取回复
     *
     * @param request 聊天请求
     * @param consumer 回复内容消费者
     */
    void streamChat(ChatRequest request, Consumer<String> consumer);

    /**
     * 流式聊天，发送消息列表并通过回调获取回复
     *
     * @param messages 消息列表
     * @param consumer 回复内容消费者
     */
    void streamChat(List<ChatMessage> messages, Consumer<String> consumer);

    /**
     * 流式聊天，发送消息列表并通过回调获取完整响应
     *
     * @param messages 消息列表
     * @param consumer 响应消费者
     */
    void streamChatFull(List<ChatMessage> messages, Consumer<ChatResponse> consumer);

    /**
     * 流式聊天，发送自定义请求并通过回调获取完整响应
     *
     * @param request 聊天请求
     * @param consumer 响应消费者
     */
    void streamChatFull(ChatRequest request, Consumer<ChatResponse> consumer);

    /**
     * 创建请求构建器
     *
     * @return 请求构建器
     */
    ChatRequest.ChatRequestBuilder createRequestBuilder();

    /**
     * 创建带系统提示的请求构建器
     *
     * @param systemPrompt 系统提示
     * @return 请求构建器
     */
    ChatRequest.ChatRequestBuilder createRequestBuilder(String systemPrompt);

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProvider();

    /**
     * 获取支持的模型列表
     *
     * @return 模型列表
     */
    List<String> getSupportedModels();

    /**
     * 生成图像
     *
     * @param prompt 提示词
     * @return 图像URL
     */
    String generateImage(String prompt);

    /**
     * 生成图像（高级选项）
     *
     * @param prompt 提示词
     * @param negativePrompt 负面提示词
     * @param width 宽度
     * @param height 高度
     * @param model 模型名称
     * @return 图像URL列表
     */
    List<String> generateImage(String prompt, String negativePrompt, int width, int height, String model);

    /**
     * 获取文本嵌入向量
     *
     * @param text 文本内容
     * @return 嵌入向量
     */
    List<Float> getEmbedding(String text);

    /**
     * 批量获取文本嵌入向量
     *
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    List<List<Float>> getEmbeddings(List<String> texts);
} 