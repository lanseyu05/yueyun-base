package online.yueyun.ip.service;

import jakarta.servlet.http.HttpServletRequest;
import online.yueyun.ip.model.IpInfo;


/**
 * IP地址检索服务接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface IpRegionService {

    /**
     * 根据IP地址查询地区信息
     *
     * @param ip IP地址
     * @return IP信息
     */
    IpInfo search(String ip);

    /**
     * 从请求中获取IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    String getIpFromRequest(HttpServletRequest request);

    /**
     * 从请求中获取IP地址并查询地区信息
     *
     * @param request HTTP请求
     * @return IP信息
     */
    IpInfo searchFromRequest(HttpServletRequest request);

    /**
     * 检查IP地址是否有效
     *
     * @param ip IP地址
     * @return 是否有效
     */
    boolean isValidIp(String ip);

    /**
     * 判断IP地址是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    boolean isInternalIp(String ip);
} 