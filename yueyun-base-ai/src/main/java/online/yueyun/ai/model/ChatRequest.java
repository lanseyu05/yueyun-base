package online.yueyun.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 聊天请求模型
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话消息列表
     */
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 模型名称
     */
    private String model;

    /**
     * 生成文本的最大令牌数
     */
    private Integer maxTokens;

    /**
     * 采样温度，控制输出的随机性 (0.0-2.0)
     */
    private Double temperature;

    /**
     * 核采样，控制输出的确定性 (0.0-1.0)
     */
    private Double topP;

    /**
     * 频率惩罚因子
     */
    private Double frequencyPenalty;

    /**
     * 存在惩罚因子
     */
    private Double presencePenalty;

    /**
     * 停止标记
     */
    private List<String> stop;

    /**
     * 函数调用定义
     */
    private Map<String, Object> functions;

    /**
     * 流式响应
     */
    private Boolean stream;

    /**
     * 自定义请求参数
     */
    private Map<String, Object> extra;

    /**
     * 添加系统消息
     *
     * @param content 消息内容
     * @return 当前请求对象
     */
    public ChatRequest addSystemMessage(String content) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(ChatMessage.system(content));
        return this;
    }

    /**
     * 添加用户消息
     *
     * @param content 消息内容
     * @return 当前请求对象
     */
    public ChatRequest addUserMessage(String content) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(ChatMessage.user(content));
        return this;
    }

    /**
     * 添加助手消息
     *
     * @param content 消息内容
     * @return 当前请求对象
     */
    public ChatRequest addAssistantMessage(String content) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(ChatMessage.assistant(content));
        return this;
    }

    /**
     * 添加函数调用结果消息
     *
     * @param name    函数名
     * @param content 函数调用结果内容
     * @return 当前请求对象
     */
    public ChatRequest addFunctionMessage(String name, String content) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(ChatMessage.function(name, content));
        return this;
    }

    /**
     * 添加带图片的用户消息
     *
     * @param content 消息内容
     * @param fileIds 图片文件ID列表
     * @return 当前请求对象
     */
    public ChatRequest addUserMessageWithImages(String content, String[] fileIds) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(ChatMessage.userWithImages(content, fileIds));
        return this;
    }
} 