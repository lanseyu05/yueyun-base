package online.yueyun.ip.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.ip.config.IpRegionProperties;
import online.yueyun.ip.model.IpInfo;
import online.yueyun.ip.service.IpRegionService;
import online.yueyun.ip.service.IpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * IP服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class IpServiceImpl implements IpService {

    private final IpRegionService ipRegionService;
    

    public IpServiceImpl(IpRegionService ipRegionService) {
        this.ipRegionService = ipRegionService;
        init();
    }
    
    @Override
    public IpInfo getIpInfo(String ip) {
        if (ip == null || ip.isEmpty()) {
            log.warn("IP地址为空");
            return IpInfo.builder().ip("").build();
        }
        
        // 使用IpRegionService查询IP信息
        return ipRegionService.search(ip);
    }

    @Override
    public IpInfo getCurrentIpInfo() {
        try {
            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("无法获取当前请求");
                return IpInfo.builder().ip("").build();
            }
            
            HttpServletRequest request = attributes.getRequest();
            return ipRegionService.searchFromRequest(request);
        } catch (Exception e) {
            log.error("获取当前IP信息失败", e);
            return IpInfo.builder().ip("").build();
        }
    }

    @Override
    public void init() {
        log.info("初始化IP服务");
        // IP库的初始化已经在IpRegionAutoConfiguration中完成
    }
} 