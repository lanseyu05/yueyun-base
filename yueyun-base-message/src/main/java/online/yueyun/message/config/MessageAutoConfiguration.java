package online.yueyun.message.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 消息服务自动配置类
 * 
 * @author yueyun
 */
@Slf4j
@EnableAsync
@Configuration
@ComponentScan("online.yueyun.message")
@EnableConfigurationProperties(MessageProperties.class)
public class MessageAutoConfiguration {

    /**
     * JSON对象映射器
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * 邮件发送器配置
     */
    @Bean
    @ConditionalOnMissingBean
    public JavaMailSender javaMailSender(MessageProperties messageProperties) {
        MessageProperties.EmailConfig emailConfig = messageProperties.getEmail();
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getHost());
        mailSender.setPort(emailConfig.getPort());
        mailSender.setUsername(emailConfig.getUsername());
        mailSender.setPassword(emailConfig.getPassword());
        mailSender.setDefaultEncoding(emailConfig.getDefaultEncoding());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        
        if (emailConfig.isSslEnabled()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.starttls.enable", "true");
        }
        
        props.put("mail.debug", "false");
        
        return mailSender;
    }

    /**
     * 消息异步线程池配置
     */
    @Bean("messageTaskExecutor")
    public Executor taskExecutor(MessageProperties messageProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(messageProperties.getAsyncCorePoolSize());
        executor.setMaxPoolSize(messageProperties.getAsyncMaxPoolSize());
        executor.setQueueCapacity(messageProperties.getAsyncQueueCapacity());
        executor.setThreadNamePrefix(messageProperties.getAsyncThreadNamePrefix());
        
        // 拒绝策略：丢弃队列最前面的任务，然后重新尝试执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 初始化
        executor.initialize();
        
        return executor;
    }
} 