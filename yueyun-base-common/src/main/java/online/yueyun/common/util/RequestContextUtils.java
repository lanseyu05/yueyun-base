package online.yueyun.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.common.config.CommonProperties;
import online.yueyun.common.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求上下文工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Component
public class RequestContextUtils {

    private final TokenUtils tokenUtils;
    private final CommonProperties properties;

    @Autowired
    public RequestContextUtils(TokenUtils tokenUtils, CommonProperties properties) {
        this.tokenUtils = tokenUtils;
        this.properties = properties;
    }

    /**
     * 获取当前请求
     *
     * @return HttpServletRequest
     */
    public HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取当前登录用户
     *
     * @return 登录用户
     */
    public LoginUser getLoginUser() {
        HttpServletRequest request = getRequest();
        return getLoginUser(request);
    }

    /**
     * 获取当前登录用户
     * @param request 请求
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
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.isAdmin();
    }

    /**
     * 从请求中获取token
     *
     * @param request HTTP请求
     * @return token
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        // 从请求头中获取token
        String header = request.getHeader(properties.getToken().getHeaderName());
        if (header != null && header.startsWith(properties.getToken().getPrefix())) {
            return header.substring(properties.getToken().getPrefix().length());
        }

        // 从Cookie中获取token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (properties.getToken().getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
} 