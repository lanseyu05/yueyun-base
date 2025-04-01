package online.yueyun.skywalking.storage;

import online.yueyun.skywalking.model.TraceData;
import online.yueyun.skywalking.model.TraceQueryCondition;

import java.util.List;

/**
 * 链路追踪存储接口
 */
public interface TraceStorage {
    
    /**
     * 存储链路追踪数据
     *
     * @param data 链路追踪数据
     */
    void store(TraceData data);
    
    /**
     * 批量存储链路追踪数据
     *
     * @param dataList 链路追踪数据列表
     */
    void storeBatch(List<TraceData> dataList);
    
    /**
     * 查询链路追踪数据
     *
     * @param traceId 追踪ID
     * @return 链路追踪数据
     */
    TraceData query(String traceId);
    
    /**
     * 查询链路追踪数据列表
     *
     * @param condition 查询条件
     * @return 链路追踪数据列表
     */
    List<TraceData> queryList(TraceQueryCondition condition);
    
    /**
     * 删除链路追踪数据
     *
     * @param traceId 追踪ID
     */
    void delete(String traceId);
    
    /**
     * 批量删除链路追踪数据
     *
     * @param traceIds 追踪ID列表
     */
    void deleteBatch(List<String> traceIds);
} 