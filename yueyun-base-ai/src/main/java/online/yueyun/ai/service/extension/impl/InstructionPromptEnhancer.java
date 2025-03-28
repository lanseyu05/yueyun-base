package online.yueyun.ai.service.extension.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.service.extension.PromptEnhancer;
import org.springframework.stereotype.Component;

/**
 * 指令型提示词增强器
 * 为提示词添加指令前缀，引导模型生成更好的回复
 * 
 * @author yueyun
 */
@Slf4j
@Component
public class InstructionPromptEnhancer implements PromptEnhancer {

    @Override
    public String enhance(String originalPrompt) {
        log.info("增强提示词，添加指令前缀，原始提示词: {}", originalPrompt);
        
        // 添加指令前缀，引导模型生成更高质量的回复
        return "请作为一名专业的助手，提供全面、准确、客观的回复。请确保回复内容符合事实，如有必要，请说明信息的来源或局限性。\n\n" +
               "用户问题：" + originalPrompt;
    }

    @Override
    public int getOrder() {
        // 设置较低优先级，应该在其他内容处理后执行
        return 100;
    }
} 