package online.yueyun.message.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import online.yueyun.message.service.MessageService;
import online.yueyun.message.service.MessageTemplateService;
import online.yueyun.message.template.MessageTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息模板控制器
 * 
 * @author yueyun
 */
@Slf4j
@RestController
@RequestMapping("/message/template")
@RequiredArgsConstructor
public class MessageTemplateController {

    private final MessageTemplateService templateService;
    private final MessageService messageService;

    /**
     * 获取所有模板列表
     * 
     * @return 模板信息列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listTemplates() {
        List<MessageTemplate> templates = templateService.getAllTemplates();
        
        List<Map<String, Object>> result = templates.stream().map(template -> {
            Map<String, Object> templateInfo = new HashMap<>();
            templateInfo.put("id", template.getTemplateId());
            templateInfo.put("name", template.getTemplateName());
            templateInfo.put("description", template.getTemplateDescription());
            templateInfo.put("defaultTitle", template.getDefaultTitle());
            templateInfo.put("requiredParams", template.getRequiredParams());
            return templateInfo;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取模板详情
     * 
     * @param templateId 模板ID
     * @return 模板详细信息
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateDetails(@PathVariable String templateId) {
        MessageTemplate template = templateService.getTemplate(templateId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", template.getTemplateId());
        result.put("name", template.getTemplateName());
        result.put("description", template.getTemplateDescription());
        result.put("defaultTitle", template.getDefaultTitle());
        result.put("requiredParams", template.getRequiredParams());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 预览模板渲染结果
     * 
     * @param templateId 模板ID
     * @param params 模板参数
     * @return 渲染后的内容
     */
    @PostMapping("/{templateId}/preview")
    public ResponseEntity<Map<String, Object>> previewTemplate(
            @PathVariable String templateId,
            @RequestBody Map<String, Object> params) {
        
        String content = templateService.renderTemplate(templateId, params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("templateId", templateId);
        result.put("content", content);
        result.put("params", params);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 使用模板发送消息
     * 
     * @param templateId 模板ID
     * @param request 请求对象，包含参数和接收者
     * @return 发送结果
     */
    @PostMapping("/{templateId}/send")
    public ResponseEntity<MessageResult> sendWithTemplate(
            @PathVariable String templateId,
            @RequestBody TemplateMessageRequest request) {
        
        // 创建消息对象
        Message message = templateService.createMessage(
                templateId, 
                request.getParams(), 
                request.getReceivers()
        );
        
        // 发送消息
        MessageResult result = messageService.send(message);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 模板消息请求对象
     */
    public static class TemplateMessageRequest {
        private Map<String, Object> params;
        private List<String> receivers;
        
        public Map<String, Object> getParams() {
            return params;
        }
        
        public void setParams(Map<String, Object> params) {
            this.params = params;
        }
        
        public List<String> getReceivers() {
            return receivers;
        }
        
        public void setReceivers(List<String> receivers) {
            this.receivers = receivers;
        }
    }
} 