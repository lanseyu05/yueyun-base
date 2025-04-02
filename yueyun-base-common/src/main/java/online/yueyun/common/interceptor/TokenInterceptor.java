package online.yueyun.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.common.annotation.SkipTokenValidation;
import online.yueyun.common.config.CommonProperties;
import online.yueyun.common.exception.BusinessException;
import online.yueyun.common.util.RequestContextUtils;
import online.yueyun.common.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token拦截器
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final TokenUtils tokenUtils;
    private final RequestContextUtils requestContextUtils;
    private final CommonProperties commonProperties;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 检查是否启用token验证
        if (!commonProperties.getToken().isEnabled()) {
            return true;
        }

        // 检查是否允许匿名访问
        if (commonProperties.getToken().isAllowAnonymous()) {
            return true;
        }

        // 检查是否有跳过token验证的注解
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            SkipTokenValidation skipTokenValidation = handlerMethod.getMethodAnnotation(SkipTokenValidation.class);
            if (skipTokenValidation != null) {
                return true;
            }
            
            // 检查类上是否有注解
            skipTokenValidation = handlerMethod.getBeanType().getAnnotation(SkipTokenValidation.class);
            if (skipTokenValidation != null) {
                return true;
            }
        }

        // 获取当前登录用户
        var loginUser = requestContextUtils.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException("未登录或token已过期");
        }

        // 获取token
        String token = requestContextUtils.getTokenFromRequest(request);
        if (token == null) {
            throw new BusinessException("未登录或token已过期");
        }

        // 检查是否需要续签
        if (tokenUtils.needRenew(token)) {
            String newToken = tokenUtils.renewToken(token);
            if (newToken != null) {
                // 更新token
                updateToken(response, newToken);
            }
        }

        return true;
    }

    /**
     * 更新token
     *
     * @param response HTTP响应
     * @param token    新token
     */
    private void updateToken(HttpServletResponse response, String token) {
        var renewConfig = commonProperties.getToken().getRenew();
        
        // 更新Cookie
        if (renewConfig.isUpdateCookie()) {
            Cookie cookie = new Cookie(commonProperties.getToken().getCookieName(), token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge((int) commonProperties.getToken().getExpireTime());
            response.addCookie(cookie);
        }

        // 更新响应头
        if (renewConfig.isUpdateHeader()) {
            response.setHeader(commonProperties.getToken().getHeaderName(), 
                commonProperties.getToken().getPrefix() + token);
        }
    }
} 