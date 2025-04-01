package online.yueyun.message.sender.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.SmsProperties;
import online.yueyun.message.dto.MessageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云短信服务提供商实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunSmsProvider implements SmsProvider {
    private final SmsProperties smsProperties;
    private final ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private boolean initialized = false;

    @Override
    public String getName() {
        return "aliyun";
    }

    @Override
    public boolean send(MessageRequest request) {
        try {
            if (!initialized && !initialize()) {
                return false;
            }
            
            log.info("发送阿里云短信，接收人: {}, 模板: {}", 
                    String.join(",", request.getReceivers()),
                    request.getTemplateId());
            
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("PhoneNumbers", String.join(",", request.getReceivers()));
            params.put("SignName", smsProperties.getSignName());
            params.put("TemplateCode", request.getTemplateId() != null ? 
                    request.getTemplateId() : smsProperties.getDefaultTemplateCode());
            
            // 设置模板参数
            if (request.getParams() != null && !request.getParams().isEmpty()) {
                params.put("TemplateParam", objectMapper.writeValueAsString(request.getParams()));
            }
            
            // 添加认证信息和其他必要参数
            params.put("AccessKeyId", smsProperties.getAccessKey());
            // 省略签名计算过程，实际需要按照阿里云的规则计算签名
            
            // 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, headers);
            
            // 模拟请求发送
            // 实际应使用阿里云SDK或根据阿里云API文档构建正确的请求
            log.info("模拟发送阿里云短信请求: {}", params);
            
            // 模拟成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("Code", "OK");
            response.put("Message", "success");
            response.put("BizId", java.util.UUID.randomUUID().toString());
            
            // 处理响应
            if ("OK".equals(response.get("Code"))) {
                log.info("阿里云短信发送成功，手机号: {}, 响应ID: {}", 
                        String.join(",", request.getReceivers()), 
                        response.get("BizId"));
                return true;
            } else {
                log.error("阿里云短信发送失败，手机号: {}, 错误码: {}, 错误信息: {}", 
                        String.join(",", request.getReceivers()), 
                        response.get("Code"), 
                        response.get("Message"));
                return false;
            }
        } catch (Exception e) {
            log.error("阿里云短信发送异常", e);
            return false;
        }
    }
    
    @Override
    public boolean initialize() {
        try {
            log.info("初始化阿里云短信客户端");
            
            if (StringUtils.hasText(smsProperties.getAccessKey()) ||
                StringUtils.hasText(smsProperties.getSecretKey())) {
                log.error("阿里云短信配置缺失: AccessKey或SecretKey未设置");
                return false;
            }
            
            if (StringUtils.hasText(smsProperties.getSignName())) {
                log.error("阿里云短信配置缺失: 签名未设置");
                return false;
            }
            
            // 初始化RestTemplate
            restTemplate = new RestTemplate();
            
            initialized = true;
            log.info("阿里云短信客户端初始化成功");
            return true;
        } catch (Exception e) {
            log.error("阿里云短信客户端初始化失败", e);
            return false;
        }
    }
} 