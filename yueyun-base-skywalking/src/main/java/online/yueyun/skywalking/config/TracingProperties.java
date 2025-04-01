package online.yueyun.skywalking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 链路追踪配置属性
 */
@Data
@ConfigurationProperties(prefix = "yueyun.tracing")
public class TracingProperties {
    
    /**
     * 是否启用链路追踪
     */
    private boolean enabled = true;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 环境
     */
    private String environment;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 采样率
     */
    private double samplingRate = 1.0;
    
    /**
     * 忽略路径
     */
    private List<String> ignorePaths;
    
    /**
     * 忽略方法
     */
    private List<String> ignoreMethods;
    
    /**
     * 忽略异常
     */
    private List<String> ignoreExceptions;
    
    /**
     * 忽略参数
     */
    private List<String> ignoreParameters;
    
    /**
     * 忽略响应
     */
    private List<String> ignoreResponses;
    
    /**
     * 忽略标签
     */
    private List<String> ignoreTags;
    
    /**
     * 忽略指标
     */
    private List<String> ignoreMetrics;
    
    /**
     * 忽略业务
     */
    private List<String> ignoreBusiness;
    
    /**
     * 忽略自定义
     */
    private List<String> ignoreCustom;
} 