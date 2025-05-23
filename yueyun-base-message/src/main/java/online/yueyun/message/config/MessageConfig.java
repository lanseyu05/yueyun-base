package online.yueyun.message.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceHttpClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 消息配置类
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class MessageConfig {
    private final MessageProperties messageProperties;

    /**
     * 异步任务执行器
     */
    @Bean("messageTaskExecutor")
    public Executor messageTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(messageProperties.getThreadPool().getCoreSize());

        // 最大线程数
        executor.setMaxPoolSize(messageProperties.getThreadPool().getMaxSize());

        // 队列容量
        executor.setQueueCapacity(messageProperties.getThreadPool().getQueueCapacity());

        // 线程名前缀
        executor.setThreadNamePrefix("message-task-");

        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 初始化
        executor.initialize();

        return executor;
    }

    /**
     * RestTemplate配置
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return new RestTemplate(factory);
    }

    /**
     * ObjectMapper配置
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }


    @Bean
    @ConditionalOnMissingBean(WxMpService.class)
    public WxMpService wxMpService() {
        return new WxMpServiceHttpClientImpl();
    }
} 