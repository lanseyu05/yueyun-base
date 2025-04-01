package online.yueyun.skywalking.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 链路追踪数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceData {
    
    /**
     * 追踪ID
     */
    private String traceId;
    
    /**
     * 片段ID
     */
    private String segmentId;
    
    /**
     * 跨度ID
     */
    private Integer spanId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 服务实例名称
     */
    private String serviceInstanceName;
    
    /**
     * 操作名称
     */
    private String operationName;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 耗时(毫秒)
     */
    private Long duration;
    
    /**
     * 是否错误
     */
    private Boolean error;
    
    /**
     * 错误类型
     */
    private String errorType;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 错误堆栈
     */
    private String errorStack;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 类名
     */
    private String className;
    
    /**
     * 方法名
     */
    private String methodName;
    
    /**
     * 参数
     */
    private String parameters;
    
    /**
     * 结果
     */
    private String result;
    
    /**
     * 标签
     */
    private Map<String, String> tags;
    
    /**
     * 指标
     */
    private Map<String, Double> metrics;
    
    /**
     * 业务数据
     */
    private Map<String, Object> business;
    
    /**
     * 自定义数据
     */
    private Map<String, Object> custom;
} 