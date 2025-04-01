package online.yueyun.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import online.yueyun.message.enums.MessageChannelEnum;

import java.util.List;
import java.util.Map;

/**
 * 消息请求DTO
 */
@Data
public class MessageRequest {
    /**
     * 消息渠道
     */
    @NotNull(message = "消息渠道不能为空")
    private MessageChannelEnum channel;

    /**
     * 消息模板ID
     */
    @NotBlank(message = "消息模板ID不能为空")
    private String templateId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收者列表
     */
    @NotEmpty(message = "接收者列表不能为空")
    private List<String> receivers;

    /**
     * 抄送者列表
     */
    private List<String> ccList;

    /**
     * 密送者列表
     */
    private List<String> bccList;

    /**
     * 附件列表
     */
    private List<Attachment> attachments;

    /**
     * 模板参数
     */
    private Map<String, Object> params;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 附件信息
     */
    @Data
    public static class Attachment {
        /**
         * 附件名称
         */
        @NotBlank(message = "附件名称不能为空")
        private String name;

        /**
         * 附件URL
         */
        @NotBlank(message = "附件URL不能为空")
        private String url;

        /**
         * 附件类型
         */
        private String contentType;
    }
} 