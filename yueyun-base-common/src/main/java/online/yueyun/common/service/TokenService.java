package online.yueyun.common.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.common.config.CommonProperties;
import online.yueyun.common.model.LoginUser;
import online.yueyun.common.util.RequestContextUtils;
import online.yueyun.common.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Token服务
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
public class TokenService {

    private final TokenUtils tokenUtils;
    private final RequestContextUtils requestContextUtils;
    private final CommonProperties properties;

    @Autowired
    public TokenService(TokenUtils tokenUtils, RequestContextUtils requestContextUtils, CommonProperties properties) {
        this.tokenUtils = tokenUtils;
        this.requestContextUtils = requestContextUtils;
        this.properties = properties;
    }

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求
     * @return 登录用户
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        return tokenUtils.parseToken(token);
    }

    /**
     * 从请求中获取token
     *
     * @param request HTTP请求
     * @return token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        return requestContextUtils.getTokenFromRequest(request);
    }

    /**
     * 设置token到响应中
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param token    token
     */
    public void setToken(HttpServletRequest request, HttpServletResponse response, String token) {
        var tokenConfig = properties.getToken();
        var securityConfig = tokenConfig.getSecurity();
        var domainConfig = tokenConfig.getDomain();
        
        // 检查是否允许在非HTTPS环境下传输
        if (!securityConfig.isAllowNonHttps() && !request.isSecure()) {
            log.warn("当前环境不支持非HTTPS传输token");
            return;
        }
        
        // 设置Cookie
        Cookie cookie = new Cookie(tokenConfig.getCookieName(), token);
        cookie.setPath("/");
        cookie.setHttpOnly(securityConfig.isHttpOnly());
        cookie.setMaxAge((int) tokenConfig.getExpireTime());
        
        // 设置SameSite属性
        if (securityConfig.isSameSite()) {
            cookie.setAttribute("SameSite", securityConfig.getSameSitePolicy());
        }

        // 设置域名
        if (domainConfig.isEnableCrossSubdomain()) {
            String domain = getDomain(request, domainConfig);
            if (domain != null) {
                cookie.setDomain(domain);
            }
        }
        
        response.addCookie(cookie);
        
        // 设置响应头
        response.setHeader(tokenConfig.getHeaderName(), tokenConfig.getPrefix() + token);
    }

    /**
     * 移除token
     *
     * @param response HTTP响应
     */
    public void removeToken(HttpServletResponse response) {
        var tokenConfig = properties.getToken();
        var securityConfig = tokenConfig.getSecurity();
        var domainConfig = tokenConfig.getDomain();
        
        // 移除Cookie
        Cookie cookie = new Cookie(tokenConfig.getCookieName(), "");
        cookie.setPath("/");
        cookie.setHttpOnly(securityConfig.isHttpOnly());
        cookie.setMaxAge(0);
        
        // 设置SameSite属性
        if (securityConfig.isSameSite()) {
            cookie.setAttribute("SameSite", securityConfig.getSameSitePolicy());
        }

        // 设置域名
        if (domainConfig.isEnableCrossSubdomain() && domainConfig.getMainDomain() != null) {
            cookie.setDomain(domainConfig.getMainDomain());
        }
        
        response.addCookie(cookie);
        
        // 移除响应头
        response.setHeader(tokenConfig.getHeaderName(), "");
    }

    /**
     * 获取域名
     *
     * @param request      HTTP请求
     * @param domainConfig 域名配置
     * @return 域名
     */
    private String getDomain(HttpServletRequest request, CommonProperties.DomainProperties domainConfig) {
        // 如果配置了主域名，直接使用
        if (domainConfig.getMainDomain() != null && !domainConfig.getMainDomain().isEmpty()) {
            return domainConfig.getMainDomain();
        }

        // 如果启用了自动检测域名
        if (domainConfig.isAutoDetectDomain()) {
            try {
                String host = request.getHeader("Host");
                if (host != null && !host.isEmpty()) {
                    // 从Host头中提取域名
                    String domain = host.split(":")[0];
                    // 如果是IP地址，不设置域名
                    if (!domain.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                        // 提取主域名（例如：从 api.example.com 提取 .example.com）
                        int lastDotIndex = domain.lastIndexOf('.');
                        if (lastDotIndex > 0) {
                            int secondLastDotIndex = domain.lastIndexOf('.', lastDotIndex - 1);
                            if (secondLastDotIndex > 0) {
                                return domain.substring(secondLastDotIndex);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取域名失败", e);
            }
        }

        return null;
    }
} 