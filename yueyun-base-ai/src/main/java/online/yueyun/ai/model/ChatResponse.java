package online.yueyun.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 聊天响应模型
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应ID
     */
    private String id;

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 创建时间戳
     */
    private Long createdAt;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 回复选项列表
     */
    private List<Choice> choices;

    /**
     * 响应来源
     */
    private String source;

    /**
     * 用量统计
     */
    private Usage usage;

    /**
     * 是否流式响应片段
     */
    private boolean streaming;

    /**
     * 附加信息
     */
    private Map<String, Object> extra;

    /**
     * 响应选项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 索引
         */
        private int index;

        /**
         * 消息
         */
        private ChatMessage message;

        /**
         * 结束原因
         */
        private String finishReason;
    }

    /**
     * 用量统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 提示令牌数
         */
        private Integer promptTokens;

        /**
         * 完成令牌数
         */
        private Integer completionTokens;

        /**
         * 总令牌数
         */
        private Integer totalTokens;
    }

    /**
     * 获取回复内容
     *
     * @return 回复内容
     */
    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return null;
    }

    /**
     * 判断是否是函数调用响应
     *
     * @return 是否函数调用
     */
    public boolean isFunctionCall() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            ChatMessage message = choices.get(0).getMessage();
            return "function_call".equals(choices.get(0).getFinishReason()) || 
                   (message.getName() != null && !message.getName().isEmpty());
        }
        return false;
    }

    /**
     * 获取函数调用名称
     *
     * @return 函数调用名称
     */
    public String getFunctionName() {
        if (isFunctionCall() && choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getName();
        }
        return null;
    }
} 