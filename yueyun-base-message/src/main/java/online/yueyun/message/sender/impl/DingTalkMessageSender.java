package online.yueyun.message.sender.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.DingTalkProperties;
import online.yueyun.message.dto.MessageRequest;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.sender.MessageSender;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉消息发送器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkMessageSender implements MessageSender {

    private final RestTemplate restTemplate;
    private final DingTalkProperties dingTalkProperties;
    private final ObjectMapper objectMapper;
    
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Override
    public boolean send(MessageRequest request) {
        try {
            // 如果没有配置钉钉机器人Webhook地址，直接返回失败
            String webhookUrl = dingTalkProperties.getRobot().getWebhookUrl();
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.error("钉钉Webhook地址未配置");
                return false;
            }
            
            // 构建钉钉消息
            Map<String, Object> message = buildDingTalkMessage(request);
            
            // 添加签名
            String secret = dingTalkProperties.getRobot().getSecret();
            if (secret != null && !secret.isEmpty()) {
                webhookUrl = addSignature(webhookUrl, secret);
            }
            
            // 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(message), headers);
            
            // 发送请求并获取响应
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(webhookUrl, httpEntity, String.class);
            String responseBody = responseEntity.getBody();
            
            // 解析响应
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            
            // 判断响应结果
            if (response != null && "0".equals(response.get("errcode").toString())) {
                log.info("钉钉消息发送成功，接收人: {}", request.getReceivers());
                return true;
            }
            
            log.error("钉钉消息发送失败，响应：{}", response);
            return false;
        } catch (Exception e) {
            log.error("发送钉钉消息异常", e);
            return false;
        }
    }
    
    /**
     * 添加签名
     * 
     * @param webhookUrl 钉钉机器人Webhook地址
     * @param secret 签名密钥
     * @return 添加签名后的URL
     */
    private String addSignature(String webhookUrl, String secret) throws Exception {
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        
        String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
        
        return webhookUrl + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    /**
     * 构建钉钉消息
     */
    private Map<String, Object> buildDingTalkMessage(MessageRequest request) {
        Map<String, Object> message = new HashMap<>();
        String messageType = determineMessageType(request);
        
        message.put("msgtype", messageType);
        
        switch (messageType) {
            case "text":
                buildTextMessage(message, request);
                break;
            case "markdown":
                buildMarkdownMessage(message, request);
                break;
            case "link":
                buildLinkMessage(message, request);
                break;
            case "actionCard":
                buildActionCardMessage(message, request);
                break;
            default:
                buildTextMessage(message, request);
        }
        
        // 添加@功能，通过手机号@指定人
        if ("text".equals(messageType) || "markdown".equals(messageType)) {
            Map<String, Object> at = new HashMap<>();
            at.put("atMobiles", request.getReceivers());
            at.put("isAtAll", request.getReceivers() == null || request.getReceivers().isEmpty());
            message.put("at", at);
        }
        
        return message;
    }
    
    /**
     * 判断消息类型
     */
    private String determineMessageType(MessageRequest request) {
        // 根据请求参数判断消息类型
        Map<String, Object> params = request.getParams();
        if (params != null && params.containsKey("msgtype")) {
            return params.get("msgtype").toString();
        }
        
        // 如果内容包含markdown格式，则认为是markdown类型
        if (request.getContent() != null && request.getContent().contains("#")) {
            return "markdown";
        }
        
        // 默认使用文本类型
        return "text";
    }
    
    /**
     * 构建文本消息
     */
    private void buildTextMessage(Map<String, Object> message, MessageRequest request) {
        Map<String, Object> text = new HashMap<>();
        text.put("content", request.getContent());
        message.put("text", text);
    }
    
    /**
     * 构建markdown消息
     */
    private void buildMarkdownMessage(Map<String, Object> message, MessageRequest request) {
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", request.getTitle());
        markdown.put("text", request.getContent());
        message.put("markdown", markdown);
    }
    
    /**
     * 构建链接消息
     */
    private void buildLinkMessage(Map<String, Object> message, MessageRequest request) {
        Map<String, Object> link = new HashMap<>();
        link.put("title", request.getTitle());
        link.put("text", request.getContent());
        
        // 从参数中获取链接地址和图片URL
        Map<String, Object> params = request.getParams();
        if (params != null) {
            if (params.containsKey("messageUrl")) {
                link.put("messageUrl", params.get("messageUrl"));
            }
            if (params.containsKey("picUrl")) {
                link.put("picUrl", params.get("picUrl"));
            }
        }
        
        message.put("link", link);
    }
    
    /**
     * 构建卡片消息
     */
    private void buildActionCardMessage(Map<String, Object> message, MessageRequest request) {
        Map<String, Object> actionCard = new HashMap<>();
        actionCard.put("title", request.getTitle());
        actionCard.put("text", request.getContent());
        
        // 从参数中获取卡片配置
        Map<String, Object> params = request.getParams();
        if (params != null) {
            if (params.containsKey("singleTitle")) {
                actionCard.put("singleTitle", params.get("singleTitle"));
            }
            if (params.containsKey("singleURL")) {
                actionCard.put("singleURL", params.get("singleURL"));
            }
            if (params.containsKey("btnOrientation")) {
                actionCard.put("btnOrientation", params.get("btnOrientation"));
            }
            if (params.containsKey("btns")) {
                actionCard.put("btns", params.get("btns"));
            }
        }
        
        message.put("actionCard", actionCard);
    }

    @Override
    public String getChannel() {
        return MessageChannelEnum.DINGTALK.getCode();
    }
} 