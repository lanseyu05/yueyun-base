package online.yueyun.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Token响应
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class TokenResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 创建TokenResponse
     *
     * @param accessToken 访问令牌
     * @param expiresIn   过期时间（秒）
     * @return TokenResponse
     */
    public static TokenResponse of(String accessToken, Long expiresIn) {
        return new TokenResponse()
                .setAccessToken(accessToken)
                .setExpiresIn(expiresIn);
    }
} 