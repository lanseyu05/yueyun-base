package online.yueyun.redis.template;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis模板包装类，简化Redis操作
 *
 * @author YueYun
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class RedisTemplateWrapper {

    /**
     * Spring Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取RedisTemplate
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
    
    /**
     * 获取值操作助手
     */
    public ValueOperations<String, Object> opsForValue() {
        return redisTemplate.opsForValue();
    }
    
    /**
     * 获取哈希操作助手
     */
    public HashOperations<String, Object, Object> opsForHash() {
        return redisTemplate.opsForHash();
    }
    
    /**
     * 获取列表操作助手
     */
    public ListOperations<String, Object> opsForList() {
        return redisTemplate.opsForList();
    }
    
    /**
     * 获取集合操作助手
     */
    public SetOperations<String, Object> opsForSet() {
        return redisTemplate.opsForSet();
    }
    
    /**
     * 获取有序集合操作助手
     */
    public ZSetOperations<String, Object> opsForZSet() {
        return redisTemplate.opsForZSet();
    }
    
    /**
     * 获取GEO操作助手
     */
    public GeoOperations<String, Object> opsForGeo() {
        return redisTemplate.opsForGeo();
    }
    
    /**
     * 获取HyperLogLog操作助手
     */
    public HyperLogLogOperations<String, Object> opsForHyperLogLog() {
        return redisTemplate.opsForHyperLogLog();
    }
    
    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间，单位：秒（-1表示永不过期，-2表示键不存在）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
    
    /**
     * 设置过期时间
     *
     * @param key 键
     * @param timeout 过期时间，单位：秒
     * @return 是否成功
     */
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 设置过期时间
     *
     * @param key 键
     * @param duration 过期时间
     * @return 是否成功
     */
    public Boolean expire(String key, Duration duration) {
        return redisTemplate.expire(key, duration.getSeconds(), TimeUnit.SECONDS);
    }
    
    /**
     * 设置过期时间点
     *
     * @param key 键
     * @param date 过期时间点
     * @return 是否成功
     */
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }
    
    /**
     * 移除过期时间
     *
     * @param key 键
     * @return 是否成功
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }
    
    /**
     * 获取key的数据类型
     *
     * @param key 键
     * @return 数据类型
     */
    public DataType type(String key) {
        return redisTemplate.type(key);
    }
    
    /**
     * 删除key
     *
     * @param key 键
     * @return 是否成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    /**
     * 批量删除key
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }
    
    /**
     * 模糊匹配删除key
     *
     * @param pattern 模式
     * @return 删除的键数量
     */
    public Long deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return redisTemplate.delete(keys);
        }
        return 0L;
    }
    
    /**
     * 模糊匹配查找keys
     *
     * @param pattern 模式
     * @return 键集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
    
    /**
     * 重命名key
     *
     * @param oldKey 旧键
     * @param newKey 新键
     * @return 是否成功
     */
    public Boolean rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
        return Boolean.TRUE;
    }
    
    /**
     * 仅当newKey不存在时重命名key
     *
     * @param oldKey 旧键
     * @param newKey 新键
     * @return 是否成功
     */
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }
    
    /**
     * 执行Lua脚本
     *
     * @param script Lua脚本
     * @param keys 键列表
     * @param args 参数列表
     * @param <T> 返回值类型
     * @return 脚本执行结果
     */
    public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }
    
    /**
     * 执行Redis事务
     *
     * @param action 事务操作
     * @return 事务结果列表
     */
    public List<Object> executePipelined(SessionCallback<?> action) {
        return redisTemplate.executePipelined(action);
    }
    
    /**
     * 在Redis事务中执行操作
     *
     * @param consumer 操作函数
     */
    public void executeWithTransaction(Consumer<RedisOperations<String, Object>> consumer) {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.multi();
                try {
                    consumer.accept(operations);
                    return operations.exec();
                } catch (Exception e) {
                    operations.discard();
                    throw e;
                }
            }
        });
    }
    
    /**
     * 设置值
     *
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        opsForValue().set(key, value);
    }
    
    /**
     * 设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间，单位：秒
     */
    public void set(String key, Object value, long timeout) {
        opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     */
    public void set(String key, Object value, Duration duration) {
        opsForValue().set(key, value, duration);
    }
    
    /**
     * 仅当key不存在时设置值
     *
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public Boolean setIfAbsent(String key, Object value) {
        return opsForValue().setIfAbsent(key, value);
    }
    
    /**
     * 仅当key不存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间，单位：秒
     * @return 是否成功
     */
    public Boolean setIfAbsent(String key, Object value, long timeout) {
        return opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 仅当key不存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     * @return 是否成功
     */
    public Boolean setIfAbsent(String key, Object value, Duration duration) {
        return opsForValue().setIfAbsent(key, value, duration);
    }
    
    /**
     * 仅当key存在时设置值
     *
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public Boolean setIfPresent(String key, Object value) {
        return opsForValue().setIfPresent(key, value);
    }
    
    /**
     * 仅当key存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间，单位：秒
     * @return 是否成功
     */
    public Boolean setIfPresent(String key, Object value, long timeout) {
        return opsForValue().setIfPresent(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 仅当key存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     * @return 是否成功
     */
    public Boolean setIfPresent(String key, Object value, Duration duration) {
        return opsForValue().setIfPresent(key, value, duration);
    }
    
    /**
     * 获取值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return opsForValue().get(key);
    }
    
    /**
     * 获取值并转换为指定类型
     *
     * @param key 键
     * @param clazz 类型
     * @param <T> 类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 批量获取值
     *
     * @param keys 键集合
     * @return 值列表
     */
    public List<Object> multiGet(Collection<String> keys) {
        return opsForValue().multiGet(keys);
    }
    
    /**
     * 批量设置值
     *
     * @param map 键值对映射
     */
    public void multiSet(Map<String, Object> map) {
        opsForValue().multiSet(map);
    }
    
    /**
     * 仅当所有key都不存在时批量设置值
     *
     * @param map 键值对映射
     * @return 是否成功
     */
    public Boolean multiSetIfAbsent(Map<String, Object> map) {
        return opsForValue().multiSetIfAbsent(map);
    }
    
    /**
     * 递增
     *
     * @param key 键
     * @param delta 递增值
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return opsForValue().increment(key, delta);
    }
    
    /**
     * 递增
     *
     * @param key 键
     * @param delta 递增值
     * @return 递增后的值
     */
    public Double increment(String key, double delta) {
        return opsForValue().increment(key, delta);
    }
    
    /**
     * 递减
     *
     * @param key 键
     * @param delta 递减值
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return opsForValue().decrement(key, delta);
    }
} 