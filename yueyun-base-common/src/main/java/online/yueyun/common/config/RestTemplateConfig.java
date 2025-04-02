package online.yueyun.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 如果项目中已经配置了RestTemplate，则不会使用此配置
 *
 * @author YueYun
 * @since 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 默认连接超时时间（毫秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    /**
     * 默认读取超时时间（毫秒）
     */
    private static final int DEFAULT_READ_TIMEOUT = 5000;

    /**
     * 配置RestTemplate
     * 如果项目中已经配置了RestTemplate，则不会使用此配置
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    /**
     * 配置ClientHttpRequestFactory
     * 如果项目中已经配置了ClientHttpRequestFactory，则不会使用此配置
     */
    @Bean
    @ConditionalOnMissingBean(ClientHttpRequestFactory.class)
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        factory.setReadTimeout(DEFAULT_READ_TIMEOUT);
        return factory;
    }
} 