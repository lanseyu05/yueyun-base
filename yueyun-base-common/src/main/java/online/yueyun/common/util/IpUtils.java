package online.yueyun.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * IP工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class IpUtils {
    /**
     * 未知IP
     */
    private static final String UNKNOWN = "unknown";
    
    /**
     * 本地IP
     */
    private static final String LOCALHOST = "127.0.0.1";
    
    /**
     * 分隔符
     */
    private static final String SEPARATOR = ",";

    /**
     * 获取真实IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getRealIp(HttpServletRequest request) {
        String ip = null;
        // 从X-Forwarded-For获取
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !UNKNOWN.equalsIgnoreCase(xForwardedFor)) {
            int index = xForwardedFor.indexOf(SEPARATOR);
            if (index != -1) {
                ip = xForwardedFor.substring(0, index);
            } else {
                ip = xForwardedFor;
            }
        }
        
        // 从Proxy-Client-IP获取
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        // 从WL-Proxy-Client-IP获取
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        // 从HTTP_CLIENT_IP获取
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        
        // 从HTTP_X_FORWARDED_FOR获取
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        // 从X-Real-IP获取
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        // 如果还是获取不到，则使用默认IP
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST.equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("获取IP地址失败", e);
                }
            }
        }
        
        // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(SEPARATOR) > 0) {
                ip = ip.substring(0, ip.indexOf(SEPARATOR));
            }
        }
        
        return ip;
    }

    /**
     * 获取IP地址列表
     *
     * @param request HTTP请求
     * @return IP地址列表
     */
    public static List<String> getIpList(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return Arrays.asList(ip.split(SEPARATOR));
        }
        return Arrays.asList(getRealIp(request));
    }

    /**
     * 判断是否是内网IP
     *
     * @param ip IP地址
     * @return 是否是内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        return ip.startsWith("10.") || 
               ip.startsWith("172.16.") || 
               ip.startsWith("172.17.") || 
               ip.startsWith("172.18.") || 
               ip.startsWith("172.19.") || 
               ip.startsWith("172.20.") || 
               ip.startsWith("172.21.") || 
               ip.startsWith("172.22.") || 
               ip.startsWith("172.23.") || 
               ip.startsWith("172.24.") || 
               ip.startsWith("172.25.") || 
               ip.startsWith("172.26.") || 
               ip.startsWith("172.27.") || 
               ip.startsWith("172.28.") || 
               ip.startsWith("172.29.") || 
               ip.startsWith("172.30.") || 
               ip.startsWith("172.31.") || 
               ip.startsWith("192.168.");
    }
} 