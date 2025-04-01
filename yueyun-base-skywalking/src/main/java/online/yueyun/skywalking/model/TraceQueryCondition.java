package online.yueyun.skywalking.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 链路追踪查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceQueryCondition {
    
    /**
     * 追踪ID列表
     */
    private List<String> traceIds;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
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
     * 最小耗时(毫秒)
     */
    private Long minDuration;
    
    /**
     * 最大耗时(毫秒)
     */
    private Long maxDuration;
    
    /**
     * 是否错误
     */
    private Boolean error;
    
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
     * 标签
     */
    private Map<String, String> tags;
    
    /**
     * 业务数据
     */
    private Map<String, Object> business;
    
    /**
     * 自定义数据
     */
    private Map<String, Object> custom;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
} 