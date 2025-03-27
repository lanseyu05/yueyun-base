package online.yueyun.ip.service;

import online.yueyun.ip.model.IpInfo;

/**
 * IP服务接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface IpService {

    /**
     * 获取IP信息
     *
     * @param ip IP地址
     * @return IP信息
     */
    IpInfo getIpInfo(String ip);

    /**
     * 获取当前请求的IP信息
     *
     * @return IP信息
     */
    IpInfo getCurrentIpInfo();

    /**
     * 初始化IP库
     */
    void init();
} 