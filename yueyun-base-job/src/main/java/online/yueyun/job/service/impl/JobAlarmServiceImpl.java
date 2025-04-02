package online.yueyun.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import online.yueyun.job.alarm.AlarmHandler;
import online.yueyun.job.converter.JobAlarmConfigConverter;
import online.yueyun.job.entity.JobAlarmConfig;
import online.yueyun.job.mapper.JobAlarmConfigMapper;
import online.yueyun.job.model.JobInfo;
import online.yueyun.job.service.JobAlarmService;
import online.yueyun.job.util.JobInfoBuilder;
import online.yueyun.job.model.JobAlarmConfigDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 任务告警服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobAlarmServiceImpl extends ServiceImpl<JobAlarmConfigMapper, JobAlarmConfig> implements JobAlarmService {

    private final JobAlarmConfigConverter jobAlarmConfigConverter = JobAlarmConfigConverter.INSTANCE;
    private final Map<Integer, AlarmHandler> handlerMap = new ConcurrentHashMap<>();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<JobAlarmConfigDTO> getAlarmConfig(Long jobId) {
        LambdaQueryWrapper<JobAlarmConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobAlarmConfig::getJobId, jobId);
        return list(wrapper).stream()
                .map(jobAlarmConfigConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAlarmConfig(Long jobId, Integer alarmType, String receivers, Integer enabled) {
        LambdaQueryWrapper<JobAlarmConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobAlarmConfig::getJobId, jobId)
               .eq(JobAlarmConfig::getAlarmType, alarmType);
        
        JobAlarmConfig config = getOne(wrapper);
        if (config == null) {
            config = new JobAlarmConfig();
            config.setJobId(jobId);
            config.setAlarmType(alarmType);
            config.setReceivers(receivers);
            config.setEnabled(enabled);
            save(config);
        } else {
            config.setReceivers(receivers);
            config.setEnabled(enabled);
            updateById(config);
        }
    }

} 