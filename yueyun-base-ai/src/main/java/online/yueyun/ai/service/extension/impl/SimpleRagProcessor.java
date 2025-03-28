package online.yueyun.ai.service.extension.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.service.extension.RagProcessor;
import org.springframework.stereotype.Component;

/**
 * 简单RAG处理器示例实现
 * 实际项目中可以替换为真实的向量数据库或知识库检索实现
 * 
 * @author yueyun
 */
@Slf4j
@Component
public class SimpleRagProcessor implements RagProcessor {

    @Override
    public String retrieveRelevantContext(String query) {
        log.info("执行简单RAG处理，查询: {}", query);
        
        // 这里只是一个简单的模拟实现
        // 实际应用中应该连接向量数据库或知识库进行检索
        if (query.contains("公司") || query.contains("企业")) {
            return "悦芸公司成立于2023年，是一家专注于人工智能技术的科技公司，" +
                   "主要产品包括智能助手、RAG知识库和智能文档等。公司总部位于北京，" +
                   "在上海、深圳和杭州设有分公司，现有员工300余人。";
        } else if (query.contains("产品") || query.contains("服务")) {
            return "悦芸科技提供的主要产品和服务包括：\n" +
                   "1. 智能对话助手：支持多轮对话和知识库检索\n" +
                   "2. RAG知识库：支持海量文档的存储、检索和问答\n" +
                   "3. 智能文档助手：辅助文档创建、编辑和管理\n" +
                   "4. AI培训服务：提供企业AI应用培训和咨询";
        } else if (query.contains("技术") || query.contains("AI") || query.contains("人工智能")) {
            return "悦芸科技的核心技术包括：\n" +
                   "1. 大规模语言模型：自研7B-100B参数规模的大语言模型\n" +
                   "2. 向量检索引擎：支持亿级向量的高效检索\n" +
                   "3. 知识图谱：构建特定领域的知识图谱\n" +
                   "4. 多模态理解：支持图像、文本、语音的多模态融合理解";
        }
        
        // 对于没有匹配到关键词的查询，返回空字符串
        return "";
    }
    
    @Override
    public int getOrder() {
        // 设置较高优先级
        return 5;
    }
} 