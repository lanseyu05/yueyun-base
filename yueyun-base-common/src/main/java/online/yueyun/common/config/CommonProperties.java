package online.yueyun.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通用配置属性
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "yueyun.common")
public class CommonProperties {
    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 应用名称
     */
    @Value("${spring.application.name:yueyun-application}")
    private String applicationName;

    /**
     * 是否开启接口文档
     */
    private boolean enableSwagger = true;

    /**
     * Token配置
     */
    private TokenProperties token = new TokenProperties();

    /**
     * 高德地图配置
     */
    private AmapProperties amap = new AmapProperties();

    /**
     * Token配置属性
     */
    @Data
    public static class TokenProperties {
        /**
         * 是否启用token验证
         */
        private boolean enabled = true;

        /**
         * 是否允许匿名访问
         */
        private boolean allowAnonymous = false;

        /**
         * token过期时间（秒）
         */
        private long expireTime = 7200;

        /**
         * token前缀
         */
        private String prefix = "Bearer ";

        /**
         * token请求头名称
         */
        private String headerName = "Authorization";

        /**
         * token Cookie名称
         */
        private String cookieName = "token";

        /**
         * 私钥路径
         */
        private String privateKeyPath = "keys/private.key";

        /**
         * 公钥路径
         */
        private String publicKeyPath = "keys/public.key";

        /**
         * 续签配置
         */
        private RenewProperties renew = new RenewProperties();

        /**
         * 安全传输配置
         */
        private SecurityProperties security = new SecurityProperties();

        /**
         * 域名配置
         */
        private DomainProperties domain = new DomainProperties();
    }

    /**
     * Token续签配置
     */
    @Data
    public static class RenewProperties {
        /**
         * 是否启用自动续签
         */
        private boolean enabled = true;

        /**
         * 续签阈值（秒）
         */
        private long threshold = 1800;

        /**
         * 是否更新Cookie
         */
        private boolean updateCookie = true;

        /**
         * 是否更新响应头
         */
        private boolean updateHeader = true;
    }

    /**
     * Token安全传输配置
     */
    @Data
    public static class SecurityProperties {
        /**
         * 是否允许在非HTTPS环境下传输
         */
        private boolean allowNonHttps = false;

        /**
         * 是否启用HttpOnly
         */
        private boolean httpOnly = true;

        /**
         * 是否启用SameSite
         */
        private boolean sameSite = true;

        /**
         * SameSite策略
         */
        private String sameSitePolicy = "Strict";
    }

    /**
     * Token域名配置
     */
    @Data
    public static class DomainProperties {
        /**
         * 是否启用跨子域名共享
         */
        private boolean enableCrossSubdomain = true;

        /**
         * 主域名
         */
        private String mainDomain;

        /**
         * 是否自动从请求中获取主域名
         */
        private boolean autoDetectDomain = true;
    }

    /**
     * 高德地图配置属性
     */
    @Data
    public static class AmapProperties {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 高德地图Key
         */
        private String key;

        /**
         * IP定位接口地址
         */
        private String ipLocationUrl = "https://restapi.amap.com/v3/ip";

        /**
         * 请求超时时间（毫秒）
         */
        private int timeout = 5000;
    }
} 