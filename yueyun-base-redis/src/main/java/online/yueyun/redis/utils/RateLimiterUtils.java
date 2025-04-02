package online.yueyun.redis.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.redisson.api.RateType;
import org.redisson.api.RateIntervalUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式限流工具类
 * 基于Redisson实现
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Component
public class RateLimiterUtils {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 初始化限流器
     *
     * @param key 限流器key
     * @param rate 每秒允许的请求数
     * @param rateInterval 时间间隔（秒）
     */
    public void initRateLimiter(String key, int rate, int rateInterval) {
        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
            // 初始化限流器，设置每秒允许的请求数和时间间隔
            rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval, RateIntervalUnit.SECONDS);
            log.info("初始化限流器成功，key: {}, rate: {}, rateInterval: {}", key, rate, rateInterval);
        } catch (Exception e) {
            log.error("初始化限流器失败，key: {}, rate: {}, rateInterval: {}", key, rate, rateInterval, e);
            throw e;
        }
    }

    /**
     * 尝试获取令牌
     *
     * @param key 限流器key
     * @param permits 需要的令牌数
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, int permits) {
        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
            return rateLimiter.tryAcquire(permits);
        } catch (Exception e) {
            log.error("获取令牌失败，key: {}, permits: {}", key, permits, e);
            return false;
        }
    }

    /**
     * 尝试获取令牌（带超时时间）
     *
     * @param key 限流器key
     * @param permits 需要的令牌数
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, int permits, long timeout, TimeUnit unit) {
        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
            return rateLimiter.tryAcquire(permits, timeout, unit);
        } catch (Exception e) {
            log.error("获取令牌失败，key: {}, permits: {}, timeout: {}, unit: {}", key, permits, timeout, unit, e);
            return false;
        }
    }
} 