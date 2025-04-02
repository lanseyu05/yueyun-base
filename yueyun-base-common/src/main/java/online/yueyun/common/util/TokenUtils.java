package online.yueyun.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.common.config.CommonProperties;
import online.yueyun.common.exception.BusinessException;
import online.yueyun.common.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT Token工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Component
public class TokenUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final CommonProperties properties;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Autowired
    public TokenUtils(CommonProperties properties) {
        this.properties = properties;
        loadKeys();
    }

    /**
     * 加载密钥
     */
    private void loadKeys() {
        try {
            this.privateKey = KeyUtils.loadPrivateKey(properties.getToken().getPrivateKeyPath());
            this.publicKey = KeyUtils.loadPublicKey(properties.getToken().getPublicKeyPath());
        } catch (Exception e) {
            log.error("加载密钥失败", e);
            throw new BusinessException("加载密钥失败");
        }
    }

    /**
     * 从请求头中获取token
     *
     * @param header 请求头
     * @return token
     */
    public String getToken(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }
        if (header.startsWith(properties.getToken().getPrefix())) {
            return header.substring(properties.getToken().getPrefix().length());
        }
        return header;
    }

    /**
     * 生成token
     *
     * @param loginUser 登录用户
     * @return token
     */
    public String createToken(LoginUser loginUser) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", loginUser.getUserId());
            claims.put("username", loginUser.getUsername());
            claims.put("nickname", loginUser.getNickname());
            claims.put("avatar", loginUser.getAvatar());
            claims.put("roles", loginUser.getRoles());
            claims.put("permissions", loginUser.getPermissions());
            claims.put("admin", loginUser.isAdmin());

            Date now = new Date();
            Date expiration = new Date(now.getTime() + properties.getToken().getExpireTime() * 1000);

            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(privateKey, SignatureAlgorithm.ES256)
                    .compact();
        } catch (Exception e) {
            log.error("生成token失败", e);
            throw new BusinessException("生成token失败");
        }
    }

    /**
     * 解析token获取用户信息
     *
     * @param token token
     * @return 用户信息
     */
    public LoginUser parseToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return null;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(claims.get("sub", Long.class));
            loginUser.setUsername(claims.get("username", String.class));
            loginUser.setNickname(claims.get("nickname", String.class));
            loginUser.setAvatar(claims.get("avatar", String.class));
            loginUser.setRoles(claims.get("roles", List.class));
            loginUser.setPermissions(claims.get("permissions", List.class));
            loginUser.setAdmin(claims.get("admin", Boolean.class));

            return loginUser;
        } catch (ExpiredJwtException e) {
            log.error("token已过期", e);
            throw new BusinessException("token已过期");
        } catch (JwtException e) {
            log.error("解析token失败", e);
            throw new BusinessException("解析token失败");
        }
    }

    /**
     * 验证token是否有效
     *
     * @param token token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取token过期时间
     *
     * @param token token
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查token是否需要续签
     *
     * @param token token
     * @return 是否需要续签
     */
    public boolean needRenew(String token) {
        if (!properties.getToken().getRenew().isEnabled()) {
            return false;
        }

        Date expiration = getExpirationDate(token);
        if (expiration == null) {
            return false;
        }

        long threshold = properties.getToken().getRenew().getThreshold() * 1000;
        return expiration.getTime() - System.currentTimeMillis() < threshold;
    }

    /**
     * 续签token
     *
     * @param token token
     * @return 续签后的token
     */
    public String renewToken(String token) {
        try {
            LoginUser loginUser = parseToken(token);
            if (loginUser == null) {
                return null;
            }
            return createToken(loginUser);
        } catch (Exception e) {
            log.error("续签token失败", e);
            return null;
        }
    }
} 