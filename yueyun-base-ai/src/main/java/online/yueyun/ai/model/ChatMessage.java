package online.yueyun.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 聊天消息模型
 *
 * @author yueyun
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息角色
     * system: 系统消息
     * user: 用户消息
     * assistant: 助手(AI)消息
     * function: 函数消息
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 函数名称(当role为function时使用)
     */
    private String name;

    /**
     * 图片文件ID列表(当需要处理图片时使用)
     */
    private String[] fileIds;

    /**
     * 创建系统消息
     *
     * @param content 内容
     * @return 系统消息
     */
    public static ChatMessage system(String content) {
        ChatMessage message = new ChatMessage();
        message.setRole("system");
        message.setContent(content);
        return message;
    }

    /**
     * 创建用户消息
     *
     * @param content 内容
     * @return 用户消息
     */
    public static ChatMessage user(String content) {
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent(content);
        return message;
    }

    /**
     * 创建助手消息
     *
     * @param content 内容
     * @return 助手消息
     */
    public static ChatMessage assistant(String content) {
        ChatMessage message = new ChatMessage();
        message.setRole("assistant");
        message.setContent(content);
        return message;
    }

    /**
     * 创建函数消息
     *
     * @param name 函数名
     * @param content 内容
     * @return 函数消息
     */
    public static ChatMessage function(String name, String content) {
        ChatMessage message = new ChatMessage();
        message.setRole("function");
        message.setName(name);
        message.setContent(content);
        return message;
    }

    /**
     * 创建带图片的用户消息
     *
     * @param content 内容
     * @param fileIds 图片文件ID列表
     * @return 带图片的用户消息
     */
    public static ChatMessage userWithImages(String content, String[] fileIds) {
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent(content);
        message.setFileIds(fileIds);
        return message;
    }
} 