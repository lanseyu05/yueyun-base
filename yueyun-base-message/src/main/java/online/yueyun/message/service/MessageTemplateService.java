package online.yueyun.message.service;

import online.yueyun.message.model.Message;
import online.yueyun.message.template.MessageTemplate;

import java.util.List;
import java.util.Map;

/**
 * 消息模板服务接口
 * 
 * @author yueyun
 */
public interface MessageTemplateService {
    
    /**
     * 根据模板ID获取模板
     * 
     * @param templateId 模板ID
     * @return 消息模板
     */
    MessageTemplate getTemplate(String templateId);
    
    /**
     * 根据模板ID和参数创建消息
     * 
     * @param templateId 模板ID
     * @param params 模板参数
     * @param receivers 接收者列表
     * @return 消息对象
     */
    Message createMessage(String templateId, Map<String, Object> params, List<String> receivers);
    
    /**
     * 注册模板
     * 
     * @param template 模板对象
     * @return 是否注册成功
     */
    boolean registerTemplate(MessageTemplate template);
    
    /**
     * 删除模板
     * 
     * @param templateId 模板ID
     * @return 是否删除成功
     */
    boolean removeTemplate(String templateId);
    
    /**
     * 获取所有已注册模板
     * 
     * @return 模板列表
     */
    List<MessageTemplate> getAllTemplates();
    
    /**
     * 检查模板是否存在
     * 
     * @param templateId 模板ID
     * @return 是否存在
     */
    boolean exists(String templateId);
    
    /**
     * 根据模板ID和参数渲染内容
     * 
     * @param templateId 模板ID
     * @param params 模板参数
     * @return 渲染后的内容
     */
    String renderTemplate(String templateId, Map<String, Object> params);
} 