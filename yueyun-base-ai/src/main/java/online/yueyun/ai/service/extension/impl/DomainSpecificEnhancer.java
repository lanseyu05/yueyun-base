package online.yueyun.ai.service.extension.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.service.extension.PromptEnhancer;
import org.springframework.stereotype.Component;

/**
 * 领域特定提示词增强器
 * 针对特定领域的查询添加专业背景知识
 * 可以根据业务需求自定义多个不同领域的增强器
 * 
 * @author yueyun
 */
@Slf4j
@Component
public class DomainSpecificEnhancer implements PromptEnhancer {

    // 金融领域关键词
    private static final String[] FINANCE_KEYWORDS = {
            "股票", "基金", "投资", "理财", "金融", "债券", "证券", "资产", "风险"
    };
    
    // 医疗领域关键词
    private static final String[] MEDICAL_KEYWORDS = {
            "医疗", "疾病", "症状", "治疗", "药物", "健康", "诊断", "病例", "医生", "医院"
    };

    @Override
    public String enhance(String originalPrompt) {
        // 检测是否属于特定领域
        if (containsAnyKeyword(originalPrompt, FINANCE_KEYWORDS)) {
            log.info("检测到金融领域提示词，应用金融领域增强");
            return enhanceFinancePrompt(originalPrompt);
        } else if (containsAnyKeyword(originalPrompt, MEDICAL_KEYWORDS)) {
            log.info("检测到医疗领域提示词，应用医疗领域增强");
            return enhanceMedicalPrompt(originalPrompt);
        }
        
        // 不属于特定领域，返回原始提示词
        return originalPrompt;
    }
    
    /**
     * 增强金融领域提示词
     */
    private String enhanceFinancePrompt(String prompt) {
        return "请作为金融领域专家，回答以下问题。请注意：\n" +
               "1. 任何金融建议仅供参考，不构成投资建议\n" +
               "2. 市场有风险，投资需谨慎\n" +
               "3. 请基于客观数据和专业知识分析\n\n" +
               prompt;
    }
    
    /**
     * 增强医疗领域提示词
     */
    private String enhanceMedicalPrompt(String prompt) {
        return "请作为医疗领域专业人士，回答以下健康相关问题。请注意：\n" +
               "1. 所提供的信息仅供参考，不能替代专业医疗建议\n" +
               "2. 严重健康问题请咨询医生或就医\n" +
               "3. 请基于医学研究和专业知识回答\n\n" +
               prompt;
    }
    
    /**
     * 检查文本是否包含任意关键词
     */
    private boolean containsAnyKeyword(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        // 设置中等优先级，在RAG处理之后，但在通用指令增强之前
        return 50;
    }
} 