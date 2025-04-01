package online.yueyun.message.sender.sms;

import online.yueyun.message.dto.MessageRequest;

/**
 * 短信服务提供商接口
 * 用于抽象不同的短信服务商实现
 */
public interface SmsProvider {
    
    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getName();
    
    /**
     * 发送短信
     *
     * @param request 消息请求
     * @return 是否发送成功
     */
    boolean send(MessageRequest request);
    
    /**
     * 初始化服务
     * 
     * @return 是否初始化成功
     */
    boolean initialize();
} 