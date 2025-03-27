package online.yueyun.redis.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLockUtil {

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 默认锁过期时间，单位：毫秒
     */
    private static final long DEFAULT_EXPIRE = 30000L;
    
    /**
     * 默认重试次数
     */
    private static final int DEFAULT_RETRY_TIMES = 3;
    
    /**
     * 默认重试间隔，单位：毫秒
     */
    private static final long DEFAULT_RETRY_INTERVAL = 100L;
    
    /**
     * 释放锁的Lua脚本
     */
    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    
    /**
     * 获取锁
     *
     * @param lockKey 锁键
     * @return 锁值，如果获取失败则返回null
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE, DEFAULT_RETRY_TIMES, DEFAULT_RETRY_INTERVAL);
    }
    
    /**
     * 获取锁
     *
     * @param lockKey 锁键
     * @param expireTime 锁过期时间，单位：毫秒
     * @return 锁值，如果获取失败则返回null
     */
    public String tryLock(String lockKey, long expireTime) {
        return tryLock(lockKey, expireTime, DEFAULT_RETRY_TIMES, DEFAULT_RETRY_INTERVAL);
    }
    
    /**
     * 获取锁
     *
     * @param lockKey 锁键
     * @param expireTime 锁过期时间，单位：毫秒
     * @param retryTimes 重试次数
     * @param retryInterval 重试间隔，单位：毫秒
     * @return 锁值，如果获取失败则返回null
     */
    public String tryLock(String lockKey, long expireTime, int retryTimes, long retryInterval) {
        // 生成锁值，使用UUID确保唯一性
        String lockValue = UUID.randomUUID().toString();
        
        // 尝试获取锁
        for (int i = 0; i < retryTimes; i++) {
            boolean success = doLock(lockKey, lockValue, expireTime);
            if (success) {
                return lockValue;
            }
            
            // 获取锁失败，等待一段时间后重试
            if (retryInterval > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 释放锁
     *
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @return 是否成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }
        
        try {
            RedisScript<Long> script = RedisScript.of(RELEASE_LOCK_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("释放锁失败: key={}, value={}", lockKey, lockValue, e);
            return false;
        }
    }
    
    /**
     * 执行获取锁操作
     *
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @param expireTime 锁过期时间，单位：毫秒
     * @return 是否成功
     */
    private boolean doLock(String lockKey, String lockValue, long expireTime) {
        try {
            RedisCallback<Boolean> callback = connection -> {
                // 使用SET命令设置锁值并设置过期时间
                return connection.set(
                        lockKey.getBytes(StandardCharsets.UTF_8),
                        lockValue.getBytes(StandardCharsets.UTF_8),
                        Expiration.milliseconds(expireTime),
                        RedisStringCommands.SetOption.SET_IF_ABSENT
                );
            };
            return Boolean.TRUE.equals(redisTemplate.execute(callback));
        } catch (Exception e) {
            log.error("获取锁失败: key={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 锁是否存在
     *
     * @param lockKey 锁键
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        try {
            return redisTemplate.hasKey(lockKey);
        } catch (Exception e) {
            log.error("检查锁状态失败: key={}", lockKey, e);
            return false;
        }
    }
} 