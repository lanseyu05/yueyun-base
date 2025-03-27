package online.yueyun.job.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

/**
 * 任务参数工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@UtilityClass
public class JobParamUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取任务参数
     *
     * @return 任务参数
     */
    public String getJobParam() {
        return XxlJobHelper.getJobParam();
    }

    /**
     * 获取任务参数并转换为指定类型
     *
     * @param clazz 目标类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public <T> T getJobParam(Class<T> clazz) {
        String param = getJobParam();
        if (param == null || param.isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(param, clazz);
        } catch (JsonProcessingException e) {
            log.error("解析任务参数失败: {}", param, e);
            return null;
        }
    }

    /**
     * 获取任务参数并转换为Map
     *
     * @return 参数Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getJobParamMap() {
        Map<String, Object> result = getJobParam(Map.class);
        return result != null ? result : Collections.emptyMap();
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", obj, e);
            return null;
        }
    }

    /**
     * 记录任务日志
     *
     * @param format 日志格式
     * @param args 参数
     */
    public void log(String format, Object... args) {
        XxlJobHelper.log(format, args);
    }

    /**
     * 设置任务执行结果为成功
     *
     * @param msg 成功消息
     */
    public void handleSuccess(String msg) {
        XxlJobHelper.handleSuccess(msg);
    }

    /**
     * 设置任务执行结果为失败
     *
     * @param msg 失败消息
     */
    public void handleFail(String msg) {
        XxlJobHelper.handleFail(msg);
    }

    /**
     * 获取分片总数
     *
     * @return 分片总数
     */
    public int getShardTotal() {
        return XxlJobHelper.getShardTotal();
    }

    /**
     * 获取分片索引
     *
     * @return 分片索引
     */
    public int getShardIndex() {
        return XxlJobHelper.getShardIndex();
    }
} 