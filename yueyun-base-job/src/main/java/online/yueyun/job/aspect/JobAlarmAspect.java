package online.yueyun.job.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.job.alarm.AlarmHandler;
import online.yueyun.job.util.JobInfoBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import online.yueyun.job.service.JobAlarmService;
import online.yueyun.job.entity.JobAlarmConfig;
import online.yueyun.job.mapper.JobAlarmConfigMapper;

import java.util.List;

/**
 * 任务告警切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class JobAlarmAspect {

    private final AlarmHandler alarmHandler;
    private final JobAlarmConfigMapper jobAlarmConfigMapper;

    /**
     * 环绕通知，处理任务执行异常时的告警
     */
    @Around("@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            return point.proceed();
        } catch (Exception e) {
            // 获取任务信息
            Long jobId = XxlJobHelper.getJobId();
            String jobName = XxlJobHelper.getJobParam();
            String jobHandler = XxlJobHelper.getJobParam();
            String jobParam = XxlJobHelper.getJobParam();

            // 记录错误日志
            log.error("任务执行失败, jobId: {}, jobName: {}, error: {}", jobId, jobName, e.getMessage());
            XxlJobHelper.log("XXL-JOB, 任务执行失败：" + e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());

            // 从数据库获取告警配置
            LambdaQueryWrapper<JobAlarmConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(JobAlarmConfig::getJobId, jobId)
                    .eq(JobAlarmConfig::getEnabled, 1);
            List<JobAlarmConfig> alarmConfigs = jobAlarmConfigMapper.selectList(wrapper);

            // 发送告警
            if (alarmConfigs != null && !alarmConfigs.isEmpty()) {
                for (JobAlarmConfig config : alarmConfigs) {
                    try {
                        alarmHandler.sendAlarm(config, JobInfoBuilder.build(jobId, jobName, jobHandler, jobParam, e.getMessage()));
                    } catch (Exception ex) {
                        log.error("发送告警失败, jobId: {}, alarmType: {}, error: {}",
                                jobId, config.getAlarmType(), ex.getMessage());
                    }
                }
            } else {
                log.info("任务未配置告警或告警已禁用, jobId: {}", jobId);
            }

            throw e;
        }
    }
} 