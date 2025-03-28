package online.yueyun.message.template.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.enums.MessageTypeEnum;
import online.yueyun.message.template.AbstractMessageTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 验证码消息模板实现
 * 用于发送验证码类型的消息
 * 
 * @author yueyun
 */
@Slf4j
@Component
public class VerificationCodeTemplate extends AbstractMessageTemplate {

    /**
     * 模板ID
     */
    private static final String TEMPLATE_ID = "verification_code";
    
    /**
     * 模板名称
     */
    private static final String TEMPLATE_NAME = "验证码模板";
    
    /**
     * 模板描述
     */
    private static final String TEMPLATE_DESCRIPTION = "用于发送各类验证码的模板，支持自定义场景和有效期";
    
    /**
     * 默认标题
     */
    private static final String DEFAULT_TITLE = "验证码信息";
    
    /**
     * 必需的参数列表
     */
    private static final List<String> REQUIRED_PARAMS = Arrays.asList("code", "scene", "expireMinutes");
    
    /**
     * 模板内容，使用Spring EL表达式
     */
    private static final String TEMPLATE_CONTENT = 
            "您的验证码是：#{code}，用于#{scene}，有效期#{expireMinutes}分钟，请勿泄露给他人。";

    /**
     * 构造函数
     */
    public VerificationCodeTemplate() {
        super(
                TEMPLATE_ID,
                TEMPLATE_NAME,
                TEMPLATE_DESCRIPTION,
                DEFAULT_TITLE,
                REQUIRED_PARAMS,
                TEMPLATE_CONTENT,
                MessageTypeEnum.VERIFICATION,
                MessageChannelEnum.SMS
        );
    }
    
    /**
     * 验证参数
     * 对于验证码消息，增加额外的参数验证
     *
     * @param params 参数列表
     * @return 是否合法
     */
    @Override
    public boolean validateParams(Map<String, Object> params) {
        boolean baseValidation = super.validateParams(params);
        
        if (!baseValidation) {
            return false;
        }
        
        // 验证码格式验证
        String code = params.get("code").toString();
        if (code.length() < 4 || code.length() > 8) {
            log.warn("验证码长度不符合要求：{}", code);
            return false;
        }
        
        // 验证有效期，必须是数字且大于0
        Object expireMinutesObj = params.get("expireMinutes");
        try {
            int expireMinutes = Integer.parseInt(expireMinutesObj.toString());
            if (expireMinutes <= 0) {
                log.warn("验证码有效期必须大于0：{}", expireMinutes);
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("验证码有效期格式错误：{}", expireMinutesObj);
            return false;
        }
        
        return true;
    }
} 