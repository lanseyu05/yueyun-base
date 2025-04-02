package online.yueyun.job.converter;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import online.yueyun.job.entity.JobAlarmConfig;
import online.yueyun.job.model.JobAlarmConfigDTO;

/**
 * 任务告警配置转换器
 */
@Mapper
public interface JobAlarmConfigConverter {
    
    JobAlarmConfigConverter INSTANCE = Mappers.getMapper(JobAlarmConfigConverter.class);
    
    /**
     * 转换为DTO
     *
     * @param entity 实体
     * @return DTO
     */
    JobAlarmConfigDTO toDTO(JobAlarmConfig entity);
    
    /**
     * 转换为实体
     *
     * @param dto DTO
     * @return 实体
     */
    JobAlarmConfig toEntity(JobAlarmConfigDTO dto);
} 