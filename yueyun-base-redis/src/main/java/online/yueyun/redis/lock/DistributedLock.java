package online.yueyun.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Component
public class DistributedLock {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁的key
     * @return 分布式锁
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 持有锁的时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        try {
            RLock lock = getLock(lockKey);
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            log.error("获取分布式锁失败: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 尝试获取锁（默认等待5秒，持有锁30秒）
     *
     * @param lockKey 锁的key
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, 5, 30, TimeUnit.SECONDS);
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        try {
            RLock lock = getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("释放分布式锁失败: {}", lockKey, e);
        }
    }

    /**
     * 使用分布式锁执行任务
     *
     * @param lockKey 锁的key
     * @param task 要执行的任务
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String lockKey, LockTask<T> task) {
        return executeWithLock(lockKey, 5, 30, TimeUnit.SECONDS, task);
    }

    /**
     * 使用分布式锁执行任务
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 持有锁的时间
     * @param timeUnit 时间单位
     * @param task 要执行的任务
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, LockTask<T> task) {
        if (tryLock(lockKey, waitTime, leaseTime, timeUnit)) {
            try {
                return task.execute();
            } finally {
                unlock(lockKey);
            }
        }
        throw new RuntimeException("获取分布式锁失败: " + lockKey);
    }

    /**
     * 分布式锁任务接口
     */
    @FunctionalInterface
    public interface LockTask<T> {
        /**
         * 执行任务
         *
         * @return 任务执行结果
         */
        T execute();
    }
} 