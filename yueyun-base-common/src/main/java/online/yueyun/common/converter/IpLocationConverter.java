package online.yueyun.common.converter;

import online.yueyun.common.dto.IpLocationDTO;
import online.yueyun.common.entity.IpLocationEntity;
import online.yueyun.common.service.IpLocationService.IpLocation;
import online.yueyun.common.util.QQWry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IP地址转换器
 *
 * @author YueYun
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface IpLocationConverter {

    IpLocationConverter INSTANCE = Mappers.getMapper(IpLocationConverter.class);

    /**
     * 将QQWry.IPLocation转换为DTO
     */
    @Mapping(target = "startIp", source = "startIp")
    @Mapping(target = "endIp", source = "endIp")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "province", source = "province")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "district", source = "district")
    @Mapping(target = "adCode", source = "adCode")
    IpLocationDTO toDTO(QQWry.IPLocation source);

    /**
     * 批量转换QQWry.IPLocation为DTO
     */
    List<IpLocationDTO> toDTOs(List<QQWry.IPLocation> sources);

    /**
     * 将DTO转换为实体
     */
    @Mapping(target = "startIp", source = "startIp")
    @Mapping(target = "endIp", source = "endIp")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "province", source = "province")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "district", source = "district")
    @Mapping(target = "adCode", source = "adCode")
    @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now())")
    IpLocationEntity toEntity(IpLocationDTO source);

    /**
     * 批量转换DTO为实体
     */
    List<IpLocationEntity> toEntities(List<IpLocationDTO> sources);

    /**
     * 将实体转换为服务层对象
     */
    @Mapping(target = "startIp", source = "startIp")
    @Mapping(target = "endIp", source = "endIp")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "province", source = "province")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "district", source = "district")
    @Mapping(target = "adCode", source = "adCode")
    IpLocation toServiceIpLocation(IpLocationEntity source);
} 