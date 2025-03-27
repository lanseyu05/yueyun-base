package online.yueyun.ip.utils;


import cn.hutool.core.net.NetUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * IP工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@UtilityClass
public class IpUtils {

    private static final Pattern IP_PATTERN = Pattern.compile("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");

    /**
     * 本地IP
     */
    public static final String LOCAL_IP = "127.0.0.1";

    /**
     * 默认IP（未知）
     */
    public static final String UNKNOWN_IP = "unknown";

    /**
     * 获取IP地址
     *
     * @param request 请求
     * @return IP地址
     */
    public String getIpAddr(HttpServletRequest request) {
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
            if (LOCAL_IP.equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("获取本机IP异常", e);
                }
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 校验IP地址是否合法
     *
     * @param ip IP地址
     * @return 是否合法
     */
    public boolean isValidIp(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 判断IP地址是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public boolean isInternalIp(String ip) {
        return NetUtil.isInnerIP(ip);
    }

    /**
     * 判断IP地址是否未知
     *
     * @param ip IP地址
     * @return 是否未知
     */
    public boolean isUnknown(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
} 