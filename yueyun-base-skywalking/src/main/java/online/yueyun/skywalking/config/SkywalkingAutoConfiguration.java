package online.yueyun.skywalking.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.skywalking.storage.TraceStorage;
import online.yueyun.skywalking.storage.impl.ElasticsearchTraceStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SkyWalking 自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({TracingProperties.class, ElasticsearchConfig.class})
@ConditionalOnProperty(prefix = "yueyun.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SkywalkingAutoConfiguration {

    /**
     * 创建默认的 Elasticsearch 存储实现
     */
    @Bean
    @ConditionalOnMissingBean(TraceStorage.class)
    @ConditionalOnProperty(prefix = "yueyun.tracing.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TraceStorage elasticsearchTraceStorage() {
        log.info("使用默认的 Elasticsearch 存储实现");
        return new ElasticsearchTraceStorage();
    }
} 