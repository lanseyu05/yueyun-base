package online.yueyun.ip.service.impl;

import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.ip.config.IpRegionProperties;
import online.yueyun.ip.model.IpInfo;
import online.yueyun.ip.service.IpRegionService;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * IP地址检索服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class Ip2RegionServiceImpl implements IpRegionService, InitializingBean {

    private static final Pattern IP_PATTERN = Pattern.compile("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");

    private Searcher searcher;
    private LRUCache<String, String> ipCache;
    private IpRegionProperties properties;

    public Ip2RegionServiceImpl(Searcher searcher, LRUCache<String, String> ipCache, IpRegionProperties properties) {
        this.searcher = searcher;
        this.ipCache = ipCache;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("IP地址检索服务初始化完成");
        if (searcher == null) {
            log.warn("IP地址检索器未初始化，可能会影响服务功能");
        }
        if (ipCache == null && properties.isEnableCache()) {
            log.warn("IP地址缓存未初始化，但启用了缓存功能");
        }
    }

    @Override
    public IpInfo search(String ip) {
        if (StrUtil.isBlank(ip) || !isValidIp(ip)) {
            log.warn("无效的IP地址: {}", ip);
            return createEmptyIpInfo(ip);
        }

        try {
            // 检查searcher是否已初始化
            if (searcher == null) {
                log.error("IP地址检索器未初始化");
                return createEmptyIpInfo(ip);
            }
            
            // 检查缓存
            String region = null;
            if (properties.isEnableCache() && ipCache != null) {
                region = ipCache.get(ip);
            }

            if (StrUtil.isBlank(region)) {
                // 未命中缓存，查询IP库
                region = searcher.search(ip);
                if (properties.isEnableCache() && ipCache != null && StrUtil.isNotBlank(region)) {
                    ipCache.put(ip, region);
                }
            }

            return parseIpInfo(ip, region);
        } catch (Exception e) {
            log.error("查询IP地址信息失败: {}", ip, e);
            return createEmptyIpInfo(ip);
        }
    }

    @Override
    public String getIpFromRequest(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (isUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    @Override
    public IpInfo searchFromRequest(HttpServletRequest request) {
        if (request == null) {
            log.warn("请求对象为空");
            return createEmptyIpInfo("");
        }
        
        String ip = getIpFromRequest(request);
        return search(ip);
    }

    @Override
    public boolean isValidIp(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    @Override
    public boolean isInternalIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return false;
        }
        return NetUtil.isInnerIP(ip);
    }

    /**
     * 判断IP是否未知
     */
    private boolean isUnknown(String ip) {
        return StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip);
    }

    /**
     * 解析IP信息
     */
    private IpInfo parseIpInfo(String ip, String region) {
        if (StrUtil.isBlank(region) || "0|0|0|0|0".equals(region)) {
            return createEmptyIpInfo(ip);
        }

        try {
            // 格式：国家|区域|省份|城市|ISP
            String[] parts = region.split("\\|");
            IpInfo.IpInfoBuilder builder = IpInfo.builder().ip(ip).rawRegion(region);

            if (parts.length >= 1) {
                builder.country(StringUtils.hasText(parts[0]) ? parts[0] : "");
            }
            if (parts.length >= 3) {
                builder.province(StringUtils.hasText(parts[2]) ? parts[2] : "");
            }
            if (parts.length >= 4) {
                builder.city(StringUtils.hasText(parts[3]) ? parts[3] : "");
            }
            if (parts.length >= 2) {
                builder.district(StringUtils.hasText(parts[1]) ? parts[1] : "");
            }
            if (parts.length >= 5) {
                builder.isp(StringUtils.hasText(parts[4]) ? parts[4] : "");
            }

            return builder.build();
        } catch (Exception e) {
            log.error("解析IP信息失败: {}, {}", ip, region, e);
            return createEmptyIpInfo(ip);
        }
    }

    /**
     * 创建空的IP信息
     */
    private IpInfo createEmptyIpInfo(String ip) {
        return IpInfo.builder()
                .ip(ip)
                .country("")
                .province("")
                .city("")
                .district("")
                .isp("")
                .build();
    }
} 