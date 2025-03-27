package online.yueyun.skywalking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 链路追踪配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "yueyun.tracing")
public class TracingProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 是否记录请求参数
     */
    private boolean logParameters = true;

    /**
     * 是否记录请求结果
     */
    private boolean logResult = true;

    /**
     * 是否记录异常
     */
    private boolean logException = true;

    /**
     * 最大日志长度
     */
    private int maxLogLength = 1000;
} 