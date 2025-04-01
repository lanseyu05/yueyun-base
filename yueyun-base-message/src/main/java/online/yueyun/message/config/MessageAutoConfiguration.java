package online.yueyun.message.config;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 消息自动配置类
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "yueyun.message", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
    MessageProperties.class,
    EmailProperties.class,
    SmsProperties.class,
    WechatProperties.class,
    DingTalkProperties.class
})
@ComponentScan(basePackages = {"online.yueyun.message"})
@MapperScan(basePackages = {"online.yueyun.message.mapper"})
@Import(MessageConfig.class)
public class MessageAutoConfiguration {
    public MessageAutoConfiguration() {
        log.info("初始化消息模块自动配置");
    }
} 