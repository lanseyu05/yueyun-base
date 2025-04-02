package online.yueyun.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.common.config.CommonProperties;
import online.yueyun.common.converter.IpLocationConverter;
import online.yueyun.common.dto.IpLocationDTO;
import online.yueyun.common.entity.IpLocationEntity;
import online.yueyun.common.mapper.IpLocationMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IP地理位置服务
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpLocationService extends ServiceImpl<IpLocationMapper, IpLocationEntity> {


    private final CommonProperties commonProperties;
    private final IpLocationConverter ipLocationConverter;

    /**
     * 缓存名称
     */
    private static final String CACHE_NAME = "ip_location";

    /**
     * 获取IP地理位置信息
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    @Cacheable(value = CACHE_NAME, key = "#ip", unless = "#result == null")
    public IpLocation getLocation(String ip) {
        if (!commonProperties.getAmap().isEnabled()) {
            log.warn("IP地理位置服务未启用");
            return null;
        }

        if (!StringUtils.hasText(ip)) {
            log.warn("IP地址不能为空");
            return null;
        }

        // 从数据库查询IP地址信息
        Optional<IpLocationEntity> entity = findIpLocation(ip);
        return entity.map(this::convertToIpLocation).orElse(null);
    }

    /**
     * 查找IP地址信息
     */
    private Optional<IpLocationEntity> findIpLocation(String ip) {
        // 先尝试精确匹配
        LambdaQueryWrapper<IpLocationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpLocationEntity::getDeleted, false)
                .le(IpLocationEntity::getStartIp, ip)
                .ge(IpLocationEntity::getEndIp, ip)
                .last("LIMIT 1");

        IpLocationEntity entity = getOne(wrapper);
        if (entity != null) {
            return Optional.of(entity);
        }

        // 如果未找到，尝试模糊匹配
        return findIpLocationByFuzzyMatch(ip);
    }

    /**
     * 模糊匹配IP地址
     */
    private Optional<IpLocationEntity> findIpLocationByFuzzyMatch(String ip) {
        try {
            long ipLong = ipToLong(ip);
            LambdaQueryWrapper<IpLocationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IpLocationEntity::getDeleted, false)
                    .apply("INET_ATON(start_ip) <= {0} AND INET_ATON(end_ip) >= {0}", ipLong)
                    .last("LIMIT 1");

            return Optional.ofNullable(getOne(wrapper));
        } catch (Exception e) {
            log.warn("IP地址格式错误: {}", ip);
            return Optional.empty();
        }
    }

    /**
     * 将IP地址转换为长整型
     */
    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address format");
        }
        return (Long.parseLong(parts[0]) << 24) |
                (Long.parseLong(parts[1]) << 16) |
                (Long.parseLong(parts[2]) << 8) |
                Long.parseLong(parts[3]);
    }

    /**
     * 将实体转换为IP地理位置信息
     */
    private IpLocation convertToIpLocation(IpLocationEntity entity) {
        IpLocation location = new IpLocation();
        location.setStartIp(entity.getStartIp());
        location.setEndIp(entity.getEndIp());
        location.setCountry(entity.getCountry());
        location.setProvince(entity.getProvince());
        location.setCity(entity.getCity());
        location.setDistrict(entity.getDistrict());
        location.setAdCode(entity.getAdCode());
        return location;
    }

    /**
     * 更新IP地址库数据
     *
     * @param dtos IP地址DTO列表
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void updateIpLocationData(List<IpLocationDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            log.warn("IP地址库数据为空，跳过更新");
            return;
        }

        try {
            // 转换为实体
            List<IpLocationEntity> entities = ipLocationConverter.toEntities(dtos);

            // 逻辑删除所有现有数据
            LambdaQueryWrapper<IpLocationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IpLocationEntity::getDeleted, false);
            List<IpLocationEntity> existingEntities = list(wrapper);
            for (IpLocationEntity entity : existingEntities) {
                entity.setDeleted(1);
                entity.setUpdateTime(LocalDateTime.now());
            }
            updateBatchById(existingEntities);

            // 批量插入新数据
            saveBatch(entities);

            log.info("IP地址库更新成功，共更新{}条记录", entities.size());
        } catch (Exception e) {
            log.error("IP地址库更新失败", e);
            throw e;
        }
    }

    /**
     * IP地理位置信息
     */
    @Data
    public static class IpLocation {
        /**
         * 起始IP
         */
        private String startIp;

        /**
         * 结束IP
         */
        private String endIp;

        /**
         * 国家
         */
        private String country;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 区县
         */
        private String district;

        /**
         * 行政区划编码
         */
        private String adCode;
    }
} 