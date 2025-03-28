package online.yueyun.message.template.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.enums.MessageTypeEnum;
import online.yueyun.message.model.Message;
import online.yueyun.message.template.AbstractMessageTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知消息模板实现
 * 用于发送系统通知类型的消息
 * 
 * @author yueyun
 */
@Slf4j
@Component
public class NotificationTemplate extends AbstractMessageTemplate {

    /**
     * 模板ID
     */
    private static final String TEMPLATE_ID = "system_notification";
    
    /**
     * 模板名称
     */
    private static final String TEMPLATE_NAME = "系统通知模板";
    
    /**
     * 模板描述
     */
    private static final String TEMPLATE_DESCRIPTION = "用于发送系统通知的模板，支持自定义通知内容和等级";
    
    /**
     * 默认标题
     */
    private static final String DEFAULT_TITLE = "系统通知";
    
    /**
     * 必需的参数列表
     */
    private static final List<String> REQUIRED_PARAMS = Arrays.asList("content", "level");
    
    /**
     * 模板内容，使用Spring EL表达式
     */
    private static final String TEMPLATE_CONTENT = 
            "[#{level}级通知] #{content}\n\n发送时间：#{sendTime}";

    /**
     * 构造函数
     */
    public NotificationTemplate() {
        super(
                TEMPLATE_ID,
                TEMPLATE_NAME,
                TEMPLATE_DESCRIPTION,
                DEFAULT_TITLE,
                REQUIRED_PARAMS,
                TEMPLATE_CONTENT,
                MessageTypeEnum.NOTIFICATION,
                MessageChannelEnum.EMAIL
        );
    }
    
    /**
     * 重写消息创建方法，增加发送时间
     * 
     * @param params 模板参数
     * @param receivers 接收者列表
     * @return 消息对象
     */
    @Override
    public Message createMessage(Map<String, Object> params, List<String> receivers) {
        if (params == null) {
            params = new HashMap<>();
        } else {
            // 创建一个新的Map，避免修改原始参数
            params = new HashMap<>(params);
        }
        
        // 添加发送时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        params.put("sendTime", LocalDateTime.now().format(formatter));
        
        // 调整通知等级格式
        if (params.containsKey("level")) {
            String level = params.get("level").toString();
            try {
                int levelValue = Integer.parseInt(level);
                String levelDesc;
                switch (levelValue) {
                    case 1: levelDesc = "低"; break;
                    case 2: levelDesc = "中"; break;
                    case 3: levelDesc = "高"; break;
                    default: levelDesc = "普通";
                }
                params.put("level", levelDesc);
            } catch (NumberFormatException e) {
                // 如果不是数字，保持原样
                log.debug("通知等级不是数字: {}", level);
            }
        }
        
        return super.createMessage(params, receivers);
    }
    
    /**
     * 验证参数
     * 
     * @param params 参数列表
     * @return 是否合法
     */
    @Override
    public boolean validateParams(Map<String, Object> params) {
        boolean baseValidation = super.validateParams(params);
        
        if (!baseValidation) {
            return false;
        }
        
        // 内容长度验证
        String content = params.get("content").toString();
        if (content.length() > 500) {
            log.warn("通知内容超出长度限制：{} > 500", content.length());
            return false;
        }
        
        return true;
    }
} 