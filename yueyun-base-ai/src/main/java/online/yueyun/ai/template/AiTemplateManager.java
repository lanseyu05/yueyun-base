package online.yueyun.ai.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * AI提示词模板管理器
 * 
 * @author yueyun
 */
@Slf4j
@Component
public class AiTemplateManager {

    /**
     * 模板存储
     */
    private final Map<String, String> templates = new HashMap<>();

    @PostConstruct
    public void init() {
        // 初始化默认模板
        registerTemplate("summary", "请为以下文本提供简明扼要的摘要：\n\n${text}");
        registerTemplate("translation", "请将以下${source_language}文本翻译成${target_language}：\n\n${text}");
        registerTemplate("analysis", "请分析以下文本并提取关键信息：\n\n${text}");
        registerTemplate("sentiment", "请分析以下文本的情感倾向，判断其是积极的、消极的还是中性的：\n\n${text}");
        registerTemplate("code_review", "请对以下代码进行审查，指出可能的bug、性能问题或改进点：\n\n```${language}\n${code}\n```");
        
        log.info("AI模板管理器初始化完成，已加载{}个默认模板", templates.size());
    }

    /**
     * 注册新模板
     *
     * @param name 模板名称
     * @param template 模板内容
     */
    public void registerTemplate(String name, String template) {
        templates.put(name, template);
    }

    /**
     * 获取模板
     *
     * @param name 模板名称
     * @return 模板内容，如果不存在则返回null
     */
    public String getTemplate(String name) {
        return templates.get(name);
    }

    /**
     * 使用变量渲染模板
     *
     * @param templateName 模板名称
     * @param variables 变量映射
     * @return 渲染后的内容
     */
    public String renderTemplate(String templateName, Map<String, Object> variables) {
        String template = getTemplate(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        // 简单的模板渲染实现
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }

    /**
     * 获取所有模板名称
     *
     * @return 模板名称集合
     */
    public Iterable<String> getTemplateNames() {
        return templates.keySet();
    }
} 