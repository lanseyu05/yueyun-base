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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

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
    
    private static final String SMS_API_ENDPOINT = "https://dysmsapi.aliyuncs.com/";
    private static final String SMS_API_VERSION = "2017-05-25";
    private static final String SMS_ACTION = "SendSms";
    private static final String SMS_REGION_ID = "cn-hangzhou";
    private static final String ALGORITHM = "HmacSHA1";
    private static final String FORMAT = "JSON";
    private static final String SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String SIGNATURE_VERSION = "1.0";

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
            
            // 构建公共请求参数
            Map<String, String> params = new TreeMap<>();
            params.put("AccessKeyId", smsProperties.getAccessKey());
            params.put("Action", SMS_ACTION);
            params.put("Format", FORMAT);
            params.put("RegionId", StringUtils.hasText(smsProperties.getRegion()) ? smsProperties.getRegion() : SMS_REGION_ID);
            params.put("SignatureMethod", SIGNATURE_METHOD);
            params.put("SignatureVersion", SIGNATURE_VERSION);
            params.put("SignatureNonce", UUID.randomUUID().toString());
            params.put("Timestamp", getISO8601Time());
            params.put("Version", SMS_API_VERSION);
            
            // 构建业务请求参数
            params.put("PhoneNumbers", String.join(",", request.getReceivers()));
            params.put("SignName", smsProperties.getSignName());
            params.put("TemplateCode", request.getTemplateId() != null ? 
                    request.getTemplateId() : smsProperties.getDefaultTemplateCode());
            
            // 设置模板参数
            if (request.getParams() != null && !request.getParams().isEmpty()) {
                params.put("TemplateParam", objectMapper.writeValueAsString(request.getParams()));
            }
            
            // 计算签名
            String signature = calculateSignature(params, smsProperties.getSecretKey());
            
            // 构建完整请求URL
            String requestUrl = buildRequestUrl(params, signature);
            
            // 发送HTTP请求
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(requestUrl, String.class);
            String responseBody = responseEntity.getBody();
            
            // 解析响应
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            
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
    
    /**
     * 计算阿里云API签名
     * 
     * @param params 请求参数
     * @param secretKey 密钥
     * @return 签名字符串
     */
    private String calculateSignature(Map<String, String> params, String secretKey) throws Exception {
        // 1. 构建规范化请求字符串
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            canonicalizedQueryString.append("&")
                .append(percentEncode(entry.getKey()))
                .append("=")
                .append(percentEncode(entry.getValue()));
        }
        
        // 2. 构建待签名字符串
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append("GET&")
            .append(percentEncode("/"))
            .append("&")
            .append(percentEncode(canonicalizedQueryString.toString().substring(1)));
        
        // 3. 计算HMAC-SHA1签名
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec((secretKey + "&").getBytes(StandardCharsets.UTF_8), ALGORITHM));
        byte[] signData = mac.doFinal(stringToSign.toString().getBytes(StandardCharsets.UTF_8));
        
        // 4. Base64编码
        return Base64.getEncoder().encodeToString(signData);
    }
    
    /**
     * 构建完整的请求URL
     * 
     * @param params 请求参数
     * @param signature 签名
     * @return 完整请求URL
     */
    private String buildRequestUrl(Map<String, String> params, String signature) throws Exception {
        StringBuilder requestUrl = new StringBuilder(SMS_API_ENDPOINT + "?Signature=");
        requestUrl.append(percentEncode(signature));
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            requestUrl.append("&")
                .append(percentEncode(entry.getKey()))
                .append("=")
                .append(percentEncode(entry.getValue()));
        }
        
        return requestUrl.toString();
    }
    
    /**
     * 获取ISO8601格式的时间戳
     */
    private String getISO8601Time() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }
    
    /**
     * 按照RFC3986规则进行URL编码
     */
    private String percentEncode(String value) throws Exception {
        return value != null ? 
            URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~") 
            : "";
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