package online.yueyun.redis.service.impl;

import lombok.RequiredArgsConstructor;
import online.yueyun.redis.service.RedisService;
import online.yueyun.redis.template.RedisTemplateWrapper;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    
    /**
     * Redis模板包装类
     */
    private final RedisTemplateWrapper redisTemplate;
    
    // ------------------ 通用操作 ------------------
    
    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
    
    @Override
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout);
    }
    
    @Override
    public Boolean expire(String key, Duration duration) {
        return redisTemplate.expire(key, duration);
    }
    
    @Override
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }
    
    @Override
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }
    
    @Override
    public DataType getType(String key) {
        return redisTemplate.type(key);
    }
    
    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    @Override
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }
    
    @Override
    public Long deleteByPattern(String pattern) {
        return redisTemplate.deleteByPattern(pattern);
    }
    
    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
    
    @Override
    public Boolean rename(String oldKey, String newKey) {
        return redisTemplate.rename(oldKey, newKey);
    }
    
    @Override
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }
    
    @Override
    public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }
    
    // ------------------ 字符串操作 ------------------
    
    @Override
    public void set(String key, Object value) {
        redisTemplate.set(key, value);
    }
    
    @Override
    public void set(String key, Object value, long timeout) {
        redisTemplate.set(key, value, timeout);
    }
    
    @Override
    public void set(String key, Object value, Duration duration) {
        redisTemplate.set(key, value, duration);
    }
    
    @Override
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.setIfAbsent(key, value);
    }
    
    @Override
    public Boolean setIfAbsent(String key, Object value, long timeout) {
        return redisTemplate.setIfAbsent(key, value, timeout);
    }
    
    @Override
    public Boolean setIfAbsent(String key, Object value, Duration duration) {
        return redisTemplate.setIfAbsent(key, value, duration);
    }
    
    @Override
    public Boolean setIfPresent(String key, Object value) {
        return redisTemplate.setIfPresent(key, value);
    }
    
    @Override
    public Boolean setIfPresent(String key, Object value, long timeout) {
        return redisTemplate.setIfPresent(key, value, timeout);
    }
    
    @Override
    public Boolean setIfPresent(String key, Object value, Duration duration) {
        return redisTemplate.setIfPresent(key, value, duration);
    }
    
    @Override
    public Object get(String key) {
        return redisTemplate.get(key);
    }
    
    @Override
    public <T> T get(String key, Class<T> clazz) {
        return redisTemplate.get(key, clazz);
    }
    
    @Override
    public List<Object> multiGet(Collection<String> keys) {
        return redisTemplate.multiGet(keys);
    }
    
    @Override
    public void multiSet(Map<String, Object> map) {
        redisTemplate.multiSet(map);
    }
    
    @Override
    public Boolean multiSetIfAbsent(Map<String, Object> map) {
        return redisTemplate.multiSetIfAbsent(map);
    }
    
    @Override
    public Long increment(String key, long delta) {
        return redisTemplate.increment(key, delta);
    }
    
    @Override
    public Double increment(String key, double delta) {
        return redisTemplate.increment(key, delta);
    }
    
    @Override
    public Long decrement(String key, long delta) {
        return redisTemplate.decrement(key, delta);
    }
    
    // ------------------ 哈希操作 ------------------
    
    @Override
    public void hSet(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }
    
    @Override
    public Boolean hSetIfAbsent(String key, Object hashKey, Object value) {
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }
    
    @Override
    public void hMultiSet(String key, Map<?, ?> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }
    
    @Override
    public Object hGet(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }
    
    @Override
    public List<Object> hMultiGet(String key, Collection<Object> hashKeys) {
        return redisTemplate.opsForHash().multiGet(key, hashKeys);
    }
    
    @Override
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }
    
    @Override
    public Boolean hHasKey(String key, Object hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }
    
    @Override
    public Long hIncrement(String key, Object hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }
    
    @Override
    public Double hIncrement(String key, Object hashKey, double delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }
    
    @Override
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }
    
    @Override
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }
    
    @Override
    public Map<Object, Object> hEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }
    
    @Override
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }
    
    // ------------------ 列表操作 ------------------
    
    @Override
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }
    
    @Override
    public Long lLeftPushAll(String key, Object... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }
    
    @Override
    public Long lLeftPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }
    
    @Override
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }
    
    @Override
    public Long lRightPushAll(String key, Object... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }
    
    @Override
    public Long lRightPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }
    
    @Override
    public Object lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }
    
    @Override
    public Object lLeftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key, timeout, unit);
    }
    
    @Override
    public Object lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }
    
    @Override
    public Object lRightPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPop(key, timeout, unit);
    }
    
    @Override
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }
    
    @Override
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }
    
    @Override
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }
    
    @Override
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }
    
    @Override
    public void lSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }
    
    @Override
    public void lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }
    
    // ------------------ 集合操作 ------------------
    
    @Override
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }
    
    @Override
    public Long sAdd(String key, Collection<Object> values) {
        return redisTemplate.opsForSet().add(key, values.toArray());
    }
    
    @Override
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }
    
    @Override
    public Object sPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }
    
    @Override
    public List<Object> sPop(String key, long count) {
        return redisTemplate.opsForSet().pop(key, count);
    }
    
    @Override
    public Boolean sMove(String sourceKey, Object value, String destinationKey) {
        return redisTemplate.opsForSet().move(sourceKey, value, destinationKey);
    }
    
    @Override
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }
    
    @Override
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }
    
    @Override
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }
    
    @Override
    public Object sRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }
    
    @Override
    public List<Object> sRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }
    
    @Override
    public Set<Object> sIntersect(String key, String otherKey) {
        return redisTemplate.opsForSet().intersect(key, otherKey);
    }
    
    @Override
    public Set<Object> sIntersect(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().intersect(key, otherKeys);
    }
    
    @Override
    public Set<Object> sUnion(String key, String otherKey) {
        return redisTemplate.opsForSet().union(key, otherKey);
    }
    
    @Override
    public Set<Object> sUnion(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }
    
    @Override
    public Set<Object> sDifference(String key, String otherKey) {
        return redisTemplate.opsForSet().difference(key, otherKey);
    }
    
    @Override
    public Set<Object> sDifference(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().difference(key, otherKeys);
    }
    
    // ------------------ 有序集合操作 ------------------
    
    @Override
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }
    
    @Override
    public Long zAdd(String key, Set<TypedTuple<Object>> tuples) {
        return redisTemplate.opsForZSet().add(key, tuples);
    }
    
    @Override
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }
    
    @Override
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }
    
    @Override
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }
    
    @Override
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }
    
    @Override
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }
    
    @Override
    public Set<TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }
    
    @Override
    public Set<TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }
    
    @Override
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }
    
    @Override
    public Set<Object> zReverseRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }
    
    @Override
    public Set<TypedTuple<Object>> zRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }
    
    @Override
    public Set<TypedTuple<Object>> zReverseRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
    }
    
    @Override
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }
    
    @Override
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }
    
    @Override
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }
    
    @Override
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }
    
    @Override
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }
    
    @Override
    public Double zIncrementScore(String key, Object value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }
} 