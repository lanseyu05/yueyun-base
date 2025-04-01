package online.yueyun.skywalking.config;

import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "yueyun.tracing.elasticsearch")
public class ElasticsearchConfig {
    
    /**
     * 是否启用
     */
    private boolean enabled = true;
    
    /**
     * 主机地址
     */
    private String host = "localhost";
    
    /**
     * 端口
     */
    private int port = 9200;
    
    /**
     * 协议
     */
    private String scheme = "http";
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 连接超时时间(毫秒)
     */
    private int connectTimeout = 5000;
    
    /**
     * 连接请求超时时间(毫秒)
     */
    private int connectionRequestTimeout = 5000;
    
    /**
     * Socket超时时间(毫秒)
     */
    private int socketTimeout = 60000;
    
    /**
     * 最大连接数
     */
    private int maxConnTotal = 30;
    
    /**
     * 每个路由的最大连接数
     */
    private int maxConnPerRoute = 10;
    
    /**
     * 创建 RestHighLevelClient
     */
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme))
        );
    }
} 