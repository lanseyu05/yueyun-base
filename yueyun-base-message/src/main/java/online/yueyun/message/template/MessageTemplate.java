package online.yueyun.message.template;

import online.yueyun.message.model.Message;

import java.util.List;
import java.util.Map;

/**
 * 消息模板接口
 * 
 * @author yueyun
 */
public interface MessageTemplate {
    
    /**
     * 获取模板ID
     * 
     * @return 模板ID
     */
    String getTemplateId();
    
    /**
     * 获取模板名称
     * 
     * @return 模板名称
     */
    String getTemplateName();
    
    /**
     * 获取模板描述
     * 
     * @return 模板描述
     */
    String getTemplateDescription();
    
    /**
     * 根据参数渲染模板内容
     * 
     * @param params 模板参数
     * @return 渲染后的内容
     */
    String render(Map<String, Object> params);
    
    /**
     * 根据模板和参数生成消息对象
     * 
     * @param params 模板参数
     * @param receivers 接收者列表
     * @return 消息对象
     */
    Message createMessage(Map<String, Object> params, List<String> receivers);
    
    /**
     * 验证参数是否合法
     * 
     * @param params 参数列表
     * @return 是否合法
     */
    boolean validateParams(Map<String, Object> params);
    
    /**
     * 获取模板所需的参数列表
     * 
     * @return 参数列表
     */
    List<String> getRequiredParams();
    
    /**
     * 获取模板默认标题
     * 
     * @return 默认标题
     */
    String getDefaultTitle();
} 