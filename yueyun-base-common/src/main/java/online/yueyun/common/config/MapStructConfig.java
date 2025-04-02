package online.yueyun.common.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MapStruct配置
 *
 * @author YueYun
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = "online.yueyun.common.converter")
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public class MapStructConfig {
} 