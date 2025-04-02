package online.yueyun.redis.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.stereotype.Component;

/**
 * Redisson工具类
 * 提供常用的Redisson操作API
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonUtils {


    private final RedissonClient redissonClient;

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
     * 获取分布式读写锁
     *
     * @param lockKey 锁的key
     * @return 分布式读写锁
     */
    public RReadWriteLock getReadWriteLock(String lockKey) {
        return redissonClient.getReadWriteLock(lockKey);
    }

    /**
     * 获取分布式信号量
     *
     * @param semaphoreKey 信号量的key
     * @return 分布式信号量
     */
    public RSemaphore getSemaphore(String semaphoreKey) {
        return redissonClient.getSemaphore(semaphoreKey);
    }

    /**
     * 获取分布式计数器
     *
     * @param counterKey 计数器的key
     * @return 分布式计数器
     */
    public RAtomicLong getAtomicLong(String counterKey) {
        return redissonClient.getAtomicLong(counterKey);
    }

    /**
     * 获取分布式Map
     *
     * @param mapKey Map的key
     * @return 分布式Map
     */
    public <K, V> RMap<K, V> getMap(String mapKey) {
        return redissonClient.getMap(mapKey);
    }

    /**
     * 获取分布式Set
     *
     * @param setKey Set的key
     * @return 分布式Set
     */
    public <T> RSet<T> getSet(String setKey) {
        return redissonClient.getSet(setKey);
    }

    /**
     * 获取分布式List
     *
     * @param listKey List的key
     * @return 分布式List
     */
    public <T> RList<T> getList(String listKey) {
        return redissonClient.getList(listKey);
    }

    /**
     * 获取分布式队列
     *
     * @param queueKey 队列的key
     * @return 分布式队列
     */
    public <T> RQueue<T> getQueue(String queueKey) {
        return redissonClient.getQueue(queueKey);
    }

    /**
     * 获取分布式延迟队列
     *
     * @param queueKey 队列的key
     * @return 分布式延迟队列
     */
    public <T> RDelayedQueue<T> getDelayedQueue(String queueKey) {
        return redissonClient.getDelayedQueue(getQueue(queueKey));
    }

    /**
     * 获取分布式布隆过滤器
     *
     * @param bloomKey 布隆过滤器的key
     * @return 分布式布隆过滤器
     */
    public RBloomFilter<String> getBloomFilter(String bloomKey) {
        return redissonClient.getBloomFilter(bloomKey);
    }

    /**
     * 获取分布式限流器
     *
     * @param rateLimiterKey 限流器的key
     * @return 分布式限流器
     */
    public RRateLimiter getRateLimiter(String rateLimiterKey) {
        return redissonClient.getRateLimiter(rateLimiterKey);
    }

    /**
     * 获取分布式Geo
     *
     * @param geoKey Geo的key
     * @return 分布式Geo
     */
    public RGeo<String> getGeo(String geoKey) {
        return redissonClient.getGeo(geoKey);
    }

    /**
     * 获取分布式BitSet
     *
     * @param bitsetKey BitSet的key
     * @return 分布式BitSet
     */
    public RBitSet getBitSet(String bitsetKey) {
        return redissonClient.getBitSet(bitsetKey);
    }

    /**
     * 获取分布式HyperLogLog
     *
     * @param hyperLogLogKey HyperLogLog的key
     * @return 分布式HyperLogLog
     */
    public RHyperLogLog<String> getHyperLogLog(String hyperLogLogKey) {
        return redissonClient.getHyperLogLog(hyperLogLogKey);
    }

    /**
     * 获取分布式Stream
     *
     * @param streamKey Stream的key
     * @return 分布式Stream
     */
    public RStream<String, String> getStream(String streamKey) {
        return redissonClient.getStream(streamKey);
    }

    /**
     * 获取分布式Topic
     *
     * @param topicKey Topic的key
     * @return 分布式Topic
     */
    public RTopic getTopic(String topicKey) {
        return redissonClient.getTopic(topicKey);
    }

    /**
     * 获取分布式远程服务
     *
     * @param serviceKey 服务的key
     * @return 分布式远程服务
     */
    public RRemoteService getRemoteService(String serviceKey) {
        return redissonClient.getRemoteService(serviceKey);
    }

    /**
     * 获取分布式调度器
     *
     * @param schedulerKey 调度器的key
     * @return 分布式调度器
     */
    public RScheduledExecutorService getExecutorService(String schedulerKey) {
        return redissonClient.getExecutorService(schedulerKey);
    }

    /**
     * 获取分布式LiveObject
     *
     * @return 分布式LiveObject
     */
    public <T> RLiveObjectService getLiveObjectService() {
        return redissonClient.getLiveObjectService();
    }
} 