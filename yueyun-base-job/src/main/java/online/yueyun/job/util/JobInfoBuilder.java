package online.yueyun.job.util;

import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.log.XxlJobFileAppender;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.job.model.JobInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JobInfo构建工具类
 */
@Slf4j
public class JobInfoBuilder {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 构建JobInfo
     *
     * @param jobId 任务ID
     * @param jobName 任务名称
     * @param jobHandler 任务处理器
     * @param jobParam 任务参数
     * @param errorMsg 错误信息
     * @return JobInfo
     */
    public static JobInfo build(Long jobId, String jobName, String jobHandler, String jobParam, String errorMsg) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(jobId);
        jobInfo.setJobName(jobName);
        jobInfo.setJobHandler(jobHandler);
        jobInfo.setJobParam(jobParam);
        jobInfo.setTriggerTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        jobInfo.setLogContent("任务执行失败，请查看任务日志");
        jobInfo.setErrorMsg(errorMsg);
        return jobInfo;
    }
    
    /**
     * 构建JobInfo（带分片信息）
     *
     * @param jobId 任务ID
     * @param jobName 任务名称
     * @param jobHandler 任务处理器
     * @param jobParam 任务参数
     * @param errorMsg 错误信息
     * @param shardIndex 分片序号
     * @param shardTotal 总分片数
     * @return JobInfo
     */
    public static JobInfo buildWithSharding(Long jobId, String jobName, String jobHandler, String jobParam, 
                                          String errorMsg, int shardIndex, int shardTotal) {
        JobInfo jobInfo = build(jobId, jobName, jobHandler, jobParam, errorMsg);
        jobInfo.setJobName(String.format("%s[%d/%d]", jobName, shardIndex + 1, shardTotal));
        return jobInfo;
    }
} 