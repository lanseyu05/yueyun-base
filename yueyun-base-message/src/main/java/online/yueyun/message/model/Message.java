package online.yueyun.message.model;

import lombok.Builder;
import lombok.Data;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.enums.MessageTypeEnum;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 消息实体
 * 
 * @author yueyun
 */
@Data
//@Builder
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息渠道
     */
    private MessageChannelEnum channel;

    /**
     * 消息类型
     */
    private MessageTypeEnum type;

    /**
     * 接收人(邮箱地址/手机号/用户ID等)
     */
    private List<String> receivers;

    /**
     * 抄送人(邮箱地址)
     */
    private List<String> ccList;

    /**
     * 密送人(邮箱地址)
     */
    private List<String> bccList;

    /**
     * 附件列表
     */
    private List<Attachment> attachments;

    /**
     * 消息模板ID
     */
    private String templateId;

    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 发送人
     */
    private String sender;

    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;

    /**
     * 附件实体
     */
    @Data
    @Builder
    public static class Attachment implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 附件名称
         */
        private String name;
        
        /**
         * 附件数据(可以是本地文件路径或URL)
         */
        private String path;
        
        /**
         * 内容类型
         */
        private String contentType;
    }
} 