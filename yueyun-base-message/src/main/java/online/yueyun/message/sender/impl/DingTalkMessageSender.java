package online.yueyun.message.sender.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.dto.MessageRequest;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.sender.MessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉消息发送器
 */
@Slf4j
@Component
public class DingTalkMessageSender implements MessageSender {

    private final RestTemplate restTemplate;
    
    @Value("${dingtalk.webhook.url:}")
    private String webhookUrl;
    
    @Value("${dingtalk.webhook.secret:}")
    private String secret;

    public DingTalkMessageSender() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public boolean send(MessageRequest request) {
        try {
            // 如果没有配置钉钉机器人Webhook地址，直接返回失败
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.error("钉钉Webhook地址未配置");
                return false;
            }
            
            // 构建钉钉消息
            Map<String, Object> message = buildDingTalkMessage(request);
            
            // 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(message, headers);
            
            // 发送请求并获取响应
            // 实际可能需要签名验证，这里简化处理
            Map<String, Object> response = restTemplate.postForObject(webhookUrl, httpEntity, Map.class);
            
            // 解析响应
            if (response != null && "0".equals(response.get("errcode").toString())) {
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
     * 构建钉钉消息
     */
    private Map<String, Object> buildDingTalkMessage(MessageRequest request) {
        Map<String, Object> message = new HashMap<>();
        
        // 根据不同类型构建不同的消息格式
        // 这里默认使用text类型消息
        message.put("msgtype", "text");
        
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("content", request.getContent());
        message.put("text", textContent);
        
        // 添加@功能，通过手机号@指定人
        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", request.getReceivers());
        at.put("isAtAll", false);
        message.put("at", at);
        
        return message;
    }

    @Override
    public String getChannel() {
        return MessageChannelEnum.DINGTALK.getCode();
    }
} 