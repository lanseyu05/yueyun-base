package online.yueyun.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 消息发送结果
 * 
 * @author yueyun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 第三方返回的结果
     */
    private Map<String, Object> thirdPartyResult;

    /**
     * 创建成功结果
     *
     * @param messageId 消息ID
     * @return 成功结果
     */
    public static MessageResult success(String messageId) {
        return MessageResult.builder()
                .success(true)
                .messageId(messageId)
                .sendTime(new Date())
                .build();
    }

    /**
     * 创建成功结果
     *
     * @param messageId 消息ID
     * @param thirdPartyResult 第三方返回结果
     * @return 成功结果
     */
    public static MessageResult success(String messageId, Map<String, Object> thirdPartyResult) {
        return MessageResult.builder()
                .success(true)
                .messageId(messageId)
                .sendTime(new Date())
                .thirdPartyResult(thirdPartyResult)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param messageId 消息ID
     * @param errorMessage 错误消息
     * @return 失败结果
     */
    public static MessageResult failure(String messageId, String errorMessage) {
        return MessageResult.builder()
                .success(false)
                .messageId(messageId)
                .sendTime(new Date())
                .errorMessage(errorMessage)
                .build();
    }
} 