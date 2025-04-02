package online.yueyun.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import online.yueyun.job.entity.JobAlarmConfig;
import online.yueyun.job.model.JobAlarmConfigDTO;

import java.util.List;

/**
 * 任务告警服务接口
 */
public interface JobAlarmService extends IService<JobAlarmConfig> {

    /**
     * 获取任务的告警配置
     *
     * @param jobId 任务ID
     * @return 告警配置列表
     */
    List<JobAlarmConfigDTO> getAlarmConfig(Long jobId);

    /**
     * 更新任务的告警配置
     *
     * @param jobId 任务ID
     * @param alarmType 告警类型
     * @param receivers 接收人
     * @param enabled 是否启用
     */
    void updateAlarmConfig(Long jobId, Integer alarmType, String receivers, Integer enabled);
} 