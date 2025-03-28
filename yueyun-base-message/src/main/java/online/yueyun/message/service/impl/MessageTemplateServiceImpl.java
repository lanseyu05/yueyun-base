package online.yueyun.message.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.exception.MessageException;
import online.yueyun.message.model.Message;
import online.yueyun.message.service.MessageTemplateService;
import online.yueyun.message.template.MessageTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 消息模板服务实现类
 * 
 * @author yueyun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final ApplicationContext applicationContext;
    
    /**
     * 模板缓存，以模板ID为键，模板对象为值
     */
    private final Map<String, MessageTemplate> templateCache = new ConcurrentHashMap<>();

    /**
     * 初始化方法，自动扫描并注册系统中所有的消息模板
     */
    @PostConstruct
    public void init() {
        // 从Spring上下文中获取所有MessageTemplate类型的Bean
        Map<String, MessageTemplate> templates = applicationContext.getBeansOfType(MessageTemplate.class);
        
        if (templates.isEmpty()) {
            log.info("未发现MessageTemplate实现类");
            return;
        }
        
        log.info("初始化消息模板，共发现{}个模板", templates.size());
        
        // 注册所有模板
        templates.values().forEach(this::registerTemplate);
    }

    @Override
    public MessageTemplate getTemplate(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        
        MessageTemplate template = templateCache.get(templateId);
        
        if (template == null) {
            log.warn("未找到ID为{}的模板", templateId);
            throw new MessageException("未找到ID为" + templateId + "的模板");
        }
        
        return template;
    }

    @Override
    public Message createMessage(String templateId, Map<String, Object> params, List<String> receivers) {
        if (receivers == null || receivers.isEmpty()) {
            throw new IllegalArgumentException("接收者列表不能为空");
        }
        
        // 获取模板并创建消息
        MessageTemplate template = getTemplate(templateId);
        return template.createMessage(params, receivers);
    }

    @Override
    public boolean registerTemplate(MessageTemplate template) {
        if (template == null) {
            log.warn("注册模板失败：模板对象为空");
            return false;
        }
        
        String templateId = template.getTemplateId();
        
        if (templateId == null || templateId.isBlank()) {
            log.warn("注册模板失败：模板ID为空");
            return false;
        }
        
        if (templateCache.containsKey(templateId)) {
            log.warn("模板ID [{}] 已存在，将进行覆盖", templateId);
        }
        
        // 将模板添加到缓存
        templateCache.put(templateId, template);
        log.info("模板 [{}] 注册成功", templateId);
        
        return true;
    }

    @Override
    public boolean removeTemplate(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            return false;
        }
        
        if (!templateCache.containsKey(templateId)) {
            log.warn("模板 [{}] 不存在，无法删除", templateId);
            return false;
        }
        
        // 从缓存中删除模板
        templateCache.remove(templateId);
        log.info("模板 [{}] 已删除", templateId);
        
        return true;
    }

    @Override
    public List<MessageTemplate> getAllTemplates() {
        return new ArrayList<>(templateCache.values());
    }

    @Override
    public boolean exists(String templateId) {
        return templateId != null && !templateId.isBlank() && templateCache.containsKey(templateId);
    }

    @Override
    public String renderTemplate(String templateId, Map<String, Object> params) {
        MessageTemplate template = getTemplate(templateId);
        return template.render(params);
    }
} 