package online.yueyun.ai.examples;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.ai.config.AiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Spring AI集成示例类
 * 仅在dev环境下生效，并且需要配置Spring AI相关属性
 * 
 * @author yueyun
 */
@Slf4j
@Component
@Profile("dev")
public class SpringAiExample implements CommandLineRunner {

    @Autowired
    private AiProperties aiProperties;
    
    @Override
    public void run(String... args) {
        log.info("======== Spring AI示例 ========");
        log.info("AI配置信息:");
        log.info("启用状态: {}", aiProperties.isEnabled());
        log.info("默认模型: {}", aiProperties.getModel().getDefaultModel());
        log.info("最大输出令牌数: {}", aiProperties.getModel().getMaxOutputTokens());
        log.info("温度参数: {}", aiProperties.getModel().getTemperature());
        
        // 这里可以添加更多Spring AI组件实际使用示例
        /*
        示例代码：使用Spring AI组件
        
        @Autowired
        private ChatClient chatClient;
        
        UserMessage userMessage = new UserMessage("写一首关于春天的诗");
        ChatResponse response = chatClient.call(
            new Prompt(List.of(userMessage), 
            PromptOptions.builder()
                .temperature(0.7f)
                .build()
            )
        );
        log.info("AI回复: {}", response.getResult().getOutput().getContent());
        */
        
        log.info("==============================");
    }
} 