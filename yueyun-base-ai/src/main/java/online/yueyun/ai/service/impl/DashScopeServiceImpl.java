package online.yueyun.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.config.AIProperties;
import online.yueyun.ai.model.ChatMessage;
import online.yueyun.ai.model.ChatRequest;
import online.yueyun.ai.model.ChatResponse;
import online.yueyun.ai.service.AIService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 阿里云DashScope服务实现（使用RestTemplate）
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class DashScopeServiceImpl implements AIService {

    private static final String GENERATION_API_PATH = "/api/v1/services/aigc/text-generation/generation";
    private static final String EMBEDDING_API_PATH = "/api/v1/services/embeddings/text-embedding/text-embedding";
    private static final String IMAGE_API_PATH = "/api/v1/services/aigc/text2image/image-synthesis";

    private final AIProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, List<String>> modelCache = new ConcurrentHashMap<>();

    public DashScopeServiceImpl(AIProperties properties) {
        this(properties, new RestTemplate());
    }
    
    public DashScopeServiceImpl(AIProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        
        if (StringUtils.isBlank(properties.getDashScope().getApiKey())) {
            throw new IllegalArgumentException("阿里云DashScope API密钥不能为空");
        }
        
        log.info("阿里云DashScope服务初始化成功");
    }

    @Override
    public String chat(String message) {
        return chat(message, properties.getDashScope().getModel().getChat());
    }

    @Override
    public String chat(String message, String model) {
        ChatRequest request = ChatRequest.builder()
                .model(model)
                .build()
                .addUserMessage(message);
        
        ChatResponse response = chat(request);
        return response.getContent();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            // 构建请求参数
            ObjectNode requestBody = buildChatRequestBody(request);
            
            // 设置请求头
            HttpHeaders headers = createHeaders();
            
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getDashScope().getEndpoint() + GENERATION_API_PATH,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseChatResponse(response.getBody());
            } else {
                log.error("DashScope请求失败: {}", response.getStatusCode());
                throw new RuntimeException("DashScope请求失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("DashScope请求异常", e);
            throw new RuntimeException("DashScope请求异常: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return chat(ChatRequest.builder().messages(messages).build());
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages, Double temperature, String model) {
        return chat(ChatRequest.builder()
                .messages(messages)
                .temperature(temperature)
                .model(model)
                .build());
    }

    @Override
    public void streamChat(String message, Consumer<String> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("响应消费者不能为空");
        }
        
        String content = chat(message);
        consumer.accept(content);
    }

    @Override
    public void streamChat(ChatRequest request, Consumer<String> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("响应消费者不能为空");
        }
        
        ChatResponse response = chat(request);
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()
                && response.getChoices().get(0).getMessage() != null) {
            consumer.accept(response.getChoices().get(0).getMessage().getContent());
        }
    }

    @Override
    public void streamChat(List<ChatMessage> messages, Consumer<String> consumer) {
        streamChat(ChatRequest.builder().messages(messages).build(), consumer);
    }

    @Override
    public void streamChatFull(List<ChatMessage> messages, Consumer<ChatResponse> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("响应消费者不能为空");
        }
        
        ChatResponse response = chat(messages);
        consumer.accept(response);
    }

    @Override
    public void streamChatFull(ChatRequest request, Consumer<ChatResponse> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("响应消费者不能为空");
        }
        
        ChatResponse response = chat(request);
        consumer.accept(response);
    }

    @Override
    public ChatRequest.ChatRequestBuilder createRequestBuilder() {
        return ChatRequest.builder()
                .model(properties.getDashScope().getModel().getChat())
                .maxTokens(properties.getRequest().getMaxTokens())
                .temperature(properties.getRequest().getTemperature())
                .topP(properties.getRequest().getTopP());
    }

    @Override
    public ChatRequest.ChatRequestBuilder createRequestBuilder(String systemPrompt) {
        return createRequestBuilder()
                .messages(Collections.singletonList(
                        ChatMessage.system(systemPrompt != null ? systemPrompt : properties.getRequest().getSystemPrompt())
                ));
    }

    @Override
    public String getProvider() {
        return "dashscope";
    }

    @Override
    public List<String> getSupportedModels() {
        if (modelCache.containsKey("chat")) {
            return modelCache.get("chat");
        }
        
        List<String> models = Arrays.asList(
                "qwen-turbo", 
                "qwen-plus", 
                "qwen-max", 
                "qwen-vl-plus",
                "qwen-vl-max",
                "qwen-long"
        );
        
        modelCache.put("chat", models);
        return models;
    }

    @Override
    public String generateImage(String prompt) {
        try {
            // 构建请求参数
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", properties.getDashScope().getModel().getImageGeneration());
            
            ObjectNode input = objectMapper.createObjectNode();
            input.put("prompt", prompt);
            requestBody.set("input", input);
            
            // 设置请求头
            HttpHeaders headers = createHeaders();
            
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getDashScope().getEndpoint() + IMAGE_API_PATH,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode output = jsonNode.path("output");
                if (!output.isMissingNode() && output.has("results") && output.path("results").isArray()
                        && output.path("results").size() > 0) {
                    return output.path("results").path(0).path("url").asText();
                }
            }
            
            log.error("生成图片失败: {}", response.getStatusCode());
            throw new RuntimeException("生成图片失败: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("生成图片异常", e);
            throw new RuntimeException("生成图片异常: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> generateImage(String prompt, String negativePrompt, int width, int height, String model) {
        try {
            // 构建请求参数
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", StringUtils.isNotBlank(model) ? 
                    model : properties.getDashScope().getModel().getImageGeneration());
            
            ObjectNode input = objectMapper.createObjectNode();
            input.put("prompt", prompt);
            
            if (StringUtils.isNotBlank(negativePrompt)) {
                input.put("negative_prompt", negativePrompt);
            }
            
            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("n", 4);
            if (width > 0 && height > 0) {
                parameters.put("size", width + "x" + height);
            }
            
            requestBody.set("input", input);
            requestBody.set("parameters", parameters);
            
            // 设置请求头
            HttpHeaders headers = createHeaders();
            
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getDashScope().getEndpoint() + IMAGE_API_PATH,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode output = jsonNode.path("output");
                if (!output.isMissingNode() && output.has("results") && output.path("results").isArray()) {
                    List<String> urls = new ArrayList<>();
                    ArrayNode results = (ArrayNode) output.path("results");
                    for (JsonNode result : results) {
                        urls.add(result.path("url").asText());
                    }
                    return urls;
                }
            }
            
            log.error("生成图片失败: {}", response.getStatusCode());
            throw new RuntimeException("生成图片失败: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("生成图片异常", e);
            throw new RuntimeException("生成图片异常: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Float> getEmbedding(String text) {
        try {
            // 构建请求参数
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", properties.getDashScope().getModel().getEmbedding());
            
            ObjectNode input = objectMapper.createObjectNode();
            ArrayNode texts = objectMapper.createArrayNode();
            texts.add(text);
            input.set("texts", texts);
            requestBody.set("input", input);
            
            // 设置请求头
            HttpHeaders headers = createHeaders();
            
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getDashScope().getEndpoint() + EMBEDDING_API_PATH,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode output = jsonNode.path("output");
                if (!output.isMissingNode() && output.has("embeddings") && output.path("embeddings").isArray()
                        && output.path("embeddings").size() > 0) {
                    JsonNode embedding = output.path("embeddings").path(0).path("embedding");
                    if (embedding.isArray()) {
                        List<Float> embeddings = new ArrayList<>();
                        for (JsonNode value : embedding) {
                            embeddings.add((float) value.asDouble());
                        }
                        return embeddings;
                    }
                }
            }
            
            log.error("获取嵌入向量失败: {}", response.getStatusCode());
            throw new RuntimeException("获取嵌入向量失败: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("获取嵌入向量异常", e);
            throw new RuntimeException("获取嵌入向量异常: " + e.getMessage(), e);
        }
    }

    @Override
    public List<List<Float>> getEmbeddings(List<String> texts) {
        try {
            // 构建请求参数
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", properties.getDashScope().getModel().getEmbedding());
            
            ObjectNode input = objectMapper.createObjectNode();
            ArrayNode textArray = objectMapper.createArrayNode();
            for (String text : texts) {
                textArray.add(text);
            }
            input.set("texts", textArray);
            requestBody.set("input", input);
            
            // 设置请求头
            HttpHeaders headers = createHeaders();
            
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getDashScope().getEndpoint() + EMBEDDING_API_PATH,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode output = jsonNode.path("output");
                if (!output.isMissingNode() && output.has("embeddings") && output.path("embeddings").isArray()) {
                    List<List<Float>> embeddingsList = new ArrayList<>();
                    ArrayNode embeddings = (ArrayNode) output.path("embeddings");
                    for (JsonNode embeddingNode : embeddings) {
                        List<Float> embeddingValues = new ArrayList<>();
                        JsonNode embedding = embeddingNode.path("embedding");
                        if (embedding.isArray()) {
                            for (JsonNode value : embedding) {
                                embeddingValues.add((float) value.asDouble());
                            }
                            embeddingsList.add(embeddingValues);
                        }
                    }
                    return embeddingsList;
                }
            }
            
            log.error("获取嵌入向量失败: {}", response.getStatusCode());
            throw new RuntimeException("获取嵌入向量失败: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("获取嵌入向量异常", e);
            throw new RuntimeException("获取嵌入向量异常: " + e.getMessage(), e);
        }
    }

    /**
     * 创建请求头
     *
     * @return HTTP请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getDashScope().getApiKey());
        return headers;
    }

    /**
     * 构建聊天请求体
     *
     * @param request 聊天请求
     * @return 请求体JSON对象
     */
    private ObjectNode buildChatRequestBody(ChatRequest request) throws JsonProcessingException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // 设置模型
        requestBody.put("model", getRequestModel(request));
        
        // 设置输入参数
        ObjectNode input = objectMapper.createObjectNode();
        ArrayNode messages = objectMapper.createArrayNode();
        
        // 添加消息
        for (ChatMessage message : request.getMessages()) {
            ObjectNode msgNode = objectMapper.createObjectNode();
            msgNode.put("role", message.getRole());
            msgNode.put("content", message.getContent());
            
            if ("function".equals(message.getRole()) && StringUtils.isNotBlank(message.getName())) {
                msgNode.put("name", message.getName());
            }
            
            if (message.getFileIds() != null && message.getFileIds().length > 0) {
                ArrayNode fileIdsNode = objectMapper.createArrayNode();
                for (String fileId : message.getFileIds()) {
                    fileIdsNode.add(fileId);
                }
                msgNode.set("file_ids", fileIdsNode);
            }
            
            messages.add(msgNode);
        }
        
        input.set("messages", messages);
        requestBody.set("input", input);
        
        // 设置参数
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("result_format", properties.getRequest().getResultFormat());
        
        if (request.getTemperature() != null) {
            parameters.put("temperature", request.getTemperature());
        } else if (properties.getRequest().getTemperature() != null) {
            parameters.put("temperature", properties.getRequest().getTemperature());
        }
        
        if (request.getTopP() != null) {
            parameters.put("top_p", request.getTopP());
        } else if (properties.getRequest().getTopP() != null) {
            parameters.put("top_p", properties.getRequest().getTopP());
        }
        
        if (request.getMaxTokens() != null) {
            parameters.put("max_tokens", request.getMaxTokens());
        } else if (properties.getRequest().getMaxTokens() != null) {
            parameters.put("max_tokens", properties.getRequest().getMaxTokens());
        }
        
        requestBody.set("parameters", parameters);
        
        return requestBody;
    }

    /**
     * 解析聊天响应
     *
     * @param responseBody 响应体字符串
     * @return 聊天响应对象
     */
    private ChatResponse parseChatResponse(String responseBody) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        
        // 提取基本信息
        String requestId = jsonNode.path("request_id").asText();
        
        // 提取输出内容
        JsonNode output = jsonNode.path("output");
        String model = output.path("model").asText();
        
        // 提取回复选项
        List<ChatResponse.Choice> choices = new ArrayList<>();
        if (output.has("choices") && output.path("choices").isArray()) {
            ArrayNode choicesNode = (ArrayNode) output.path("choices");
            for (int i = 0; i < choicesNode.size(); i++) {
                JsonNode choiceNode = choicesNode.get(i);
                
                JsonNode messageNode = choiceNode.path("message");
                String role = messageNode.path("role").asText();
                String content = messageNode.path("content").asText();
                
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setRole(role);
                chatMessage.setContent(content);
                
                ChatResponse.Choice choice = new ChatResponse.Choice();
                choice.setIndex(i);
                choice.setMessage(chatMessage);
                choice.setFinishReason(choiceNode.path("finish_reason").asText());
                
                choices.add(choice);
            }
        }
        
        // 提取使用量统计
        ChatResponse.Usage usage = null;
        if (jsonNode.has("usage")) {
            JsonNode usageNode = jsonNode.path("usage");
            usage = new ChatResponse.Usage();
            usage.setPromptTokens(usageNode.path("input_tokens").asInt());
            usage.setCompletionTokens(usageNode.path("output_tokens").asInt());
            usage.setTotalTokens(usageNode.path("total_tokens").asInt());
        }
        
        // 创建响应
        return ChatResponse.builder()
                .id(requestId)
                .conversationId(null) // DashScope没有会话ID
                .createdAt(System.currentTimeMillis() / 1000)
                .model(model)
                .choices(choices)
                .usage(usage)
                .source("dashscope")
                .build();
    }

    /**
     * 获取请求使用的模型
     *
     * @param request 请求
     * @return 模型名称
     */
    private String getRequestModel(ChatRequest request) {
        if (StringUtils.isNotBlank(request.getModel())) {
            return request.getModel();
        }
        return properties.getDashScope().getModel().getChat();
    }
} 