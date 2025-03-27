package online.yueyun.redis.service;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务接口
 * 提供常用的Redis操作方法
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface RedisService {

    // ------------------ 通用操作 ------------------

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间，单位：秒（-1表示永不过期，-2表示键不存在）
     */
    Long getExpire(String key);

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param timeout 过期时间，单位：秒
     * @return 是否成功
     */
    Boolean expire(String key, long timeout);

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param duration 过期时间
     * @return 是否成功
     */
    Boolean expire(String key, Duration duration);

    /**
     * 设置过期时间点
     *
     * @param key 键
     * @param date 过期时间点
     * @return 是否成功
     */
    Boolean expireAt(String key, Date date);

    /**
     * 移除过期时间
     *
     * @param key 键
     * @return 是否成功
     */
    Boolean persist(String key);

    /**
     * 获取key的数据类型
     *
     * @param key 键
     * @return 数据类型
     */
    DataType getType(String key);

    /**
     * 删除key
     *
     * @param key 键
     * @return 是否成功
     */
    Boolean delete(String key);

    /**
     * 批量删除key
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    Long delete(Collection<String> keys);

    /**
     * 模糊匹配删除key
     *
     * @param pattern 模式
     * @return 删除的键数量
     */
    Long deleteByPattern(String pattern);

    /**
     * 模糊匹配查找keys
     *
     * @param pattern 模式
     * @return 键集合
     */
    Set<String> keys(String pattern);

    /**
     * 重命名key
     *
     * @param oldKey 旧键
     * @param newKey 新键
     * @return 是否成功
     */
    Boolean rename(String oldKey, String newKey);

    /**
     * 仅当newKey不存在时重命名key
     *
     * @param oldKey 旧键
     * @param newKey 新键
     * @return 是否成功
     */
    Boolean renameIfAbsent(String oldKey, String newKey);

    /**
     * 执行Lua脚本
     *
     * @param script Lua脚本
     * @param keys 键列表
     * @param args 参数列表
     * @param <T> 返回值类型
     * @return 脚本执行结果
     */
    <T> T execute(RedisScript<T> script, List<String> keys, Object... args);

    // ------------------ 字符串操作 ------------------

    /**
     * 设置值
     *
     * @param key 键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间，单位：秒
     */
    void set(String key, Object value, long timeout);

    /**
     * 设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     */
    void set(String key, Object value, Duration duration);

    /**
     * 仅当key不存在时设置值
     *
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    Boolean setIfAbsent(String key, Object value);

    /**
     * 仅当key不存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间，单位：秒
     * @return 是否成功
     */
    Boolean setIfAbsent(String key, Object value, long timeout);

    /**
     * 仅当key不存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     * @return 是否成功
     */
    Boolean setIfAbsent(String key, Object value, Duration duration);

    /**
     * 仅当key存在时设置值
     *
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    Boolean setIfPresent(String key, Object value);

    /**
     * 仅当key存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间，单位：秒
     * @return 是否成功
     */
    Boolean setIfPresent(String key, Object value, long timeout);

    /**
     * 仅当key存在时设置值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     * @return 是否成功
     */
    Boolean setIfPresent(String key, Object value, Duration duration);

    /**
     * 获取值
     *
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * 获取值并转换为指定类型
     *
     * @param key 键
     * @param clazz 类型
     * @param <T> 类型
     * @return 值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 批量获取值
     *
     * @param keys 键集合
     * @return 值列表
     */
    List<Object> multiGet(Collection<String> keys);

    /**
     * 批量设置值
     *
     * @param map 键值对映射
     */
    void multiSet(Map<String, Object> map);

    /**
     * 仅当所有key都不存在时批量设置值
     *
     * @param map 键值对映射
     * @return 是否成功
     */
    Boolean multiSetIfAbsent(Map<String, Object> map);

    /**
     * 递增
     *
     * @param key 键
     * @param delta 递增值
     * @return 递增后的值
     */
    Long increment(String key, long delta);

    /**
     * 递增
     *
     * @param key 键
     * @param delta 递增值
     * @return 递增后的值
     */
    Double increment(String key, double delta);

    /**
     * 递减
     *
     * @param key 键
     * @param delta 递减值
     * @return 递减后的值
     */
    Long decrement(String key, long delta);

    // ------------------ 哈希操作 ------------------

    /**
     * 设置哈希值
     *
     * @param key 键
     * @param hashKey 哈希键
     * @param value 值
     */
    void hSet(String key, Object hashKey, Object value);

    /**
     * 仅当hashKey不存在时设置哈希值
     *
     * @param key 键
     * @param hashKey 哈希键
     * @param value 值
     * @return 是否成功
     */
    Boolean hSetIfAbsent(String key, Object hashKey, Object value);

    /**
     * 批量设置哈希值
     *
     * @param key 键
     * @param map 哈希键值对映射
     */
    void hMultiSet(String key, Map<?, ?> map);

    /**
     * 获取哈希值
     *
     * @param key 键
     * @param hashKey 哈希键
     * @return 值
     */
    Object hGet(String key, Object hashKey);

    /**
     * 批量获取哈希值
     *
     * @param key 键
     * @param hashKeys 哈希键集合
     * @return 值列表
     */
    List<Object> hMultiGet(String key, Collection<Object> hashKeys);

    /**
     * 删除哈希键
     *
     * @param key 键
     * @param hashKeys 哈希键数组
     * @return 删除的哈希键数量
     */
    Long hDelete(String key, Object... hashKeys);

    /**
     * 判断哈希键是否存在
     *
     * @param key 键
     * @param hashKey 哈希键
     * @return 是否存在
     */
    Boolean hHasKey(String key, Object hashKey);

    /**
     * 递增哈希值
     *
     * @param key 键
     * @param hashKey 哈希键
     * @param delta 递增值
     * @return 递增后的值
     */
    Long hIncrement(String key, Object hashKey, long delta);

    /**
     * 递增哈希值
     *
     * @param key 键
     * @param hashKey 哈希键
     * @param delta 递增值
     * @return 递增后的值
     */
    Double hIncrement(String key, Object hashKey, double delta);

    /**
     * 获取哈希键集合
     *
     * @param key 键
     * @return 哈希键集合
     */
    Set<Object> hKeys(String key);

    /**
     * 获取哈希值集合
     *
     * @param key 键
     * @return 哈希值集合
     */
    List<Object> hValues(String key);

    /**
     * 获取哈希键值对映射
     *
     * @param key 键
     * @return 哈希键值对映射
     */
    Map<Object, Object> hEntries(String key);

    /**
     * 获取哈希大小
     *
     * @param key 键
     * @return 哈希大小
     */
    Long hSize(String key);

    // ------------------ 列表操作 ------------------

    /**
     * 左Push
     *
     * @param key 键
     * @param value 值
     * @return 列表大小
     */
    Long lLeftPush(String key, Object value);

    /**
     * 左批量Push
     *
     * @param key 键
     * @param values 值数组
     * @return 列表大小
     */
    Long lLeftPushAll(String key, Object... values);

    /**
     * 左批量Push
     *
     * @param key 键
     * @param values 值集合
     * @return 列表大小
     */
    Long lLeftPushAll(String key, Collection<Object> values);

    /**
     * 右Push
     *
     * @param key 键
     * @param value 值
     * @return 列表大小
     */
    Long lRightPush(String key, Object value);

    /**
     * 右批量Push
     *
     * @param key 键
     * @param values 值数组
     * @return 列表大小
     */
    Long lRightPushAll(String key, Object... values);

    /**
     * 右批量Push
     *
     * @param key 键
     * @param values 值集合
     * @return 列表大小
     */
    Long lRightPushAll(String key, Collection<Object> values);

    /**
     * 左Pop
     *
     * @param key 键
     * @return 值
     */
    Object lLeftPop(String key);

    /**
     * 左Pop并阻塞
     *
     * @param key 键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 值
     */
    Object lLeftPop(String key, long timeout, TimeUnit unit);

    /**
     * 右Pop
     *
     * @param key 键
     * @return 值
     */
    Object lRightPop(String key);

    /**
     * 右Pop并阻塞
     *
     * @param key 键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 值
     */
    Object lRightPop(String key, long timeout, TimeUnit unit);

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 列表长度
     */
    Long lSize(String key);

    /**
     * 获取列表元素
     *
     * @param key 键
     * @param index 索引
     * @return 值
     */
    Object lIndex(String key, long index);

    /**
     * 获取列表元素
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 值列表
     */
    List<Object> lRange(String key, long start, long end);

    /**
     * 移除列表元素
     *
     * @param key 键
     * @param count 数量
     * @param value 值
     * @return 移除的元素数量
     */
    Long lRemove(String key, long count, Object value);

    /**
     * 设置列表元素
     *
     * @param key 键
     * @param index 索引
     * @param value 值
     */
    void lSet(String key, long index, Object value);

    /**
     * 截断列表
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     */
    void lTrim(String key, long start, long end);

    // ------------------ 集合操作 ------------------

    /**
     * 添加元素
     *
     * @param key 键
     * @param values 值数组
     * @return 添加的元素数量
     */
    Long sAdd(String key, Object... values);

    /**
     * 批量添加元素
     *
     * @param key 键
     * @param values 值集合
     * @return 添加的元素数量
     */
    Long sAdd(String key, Collection<Object> values);

    /**
     * 删除元素
     *
     * @param key 键
     * @param values 值数组
     * @return 删除的元素数量
     */
    Long sRemove(String key, Object... values);

    /**
     * 随机弹出一个元素
     *
     * @param key 键
     * @return 值
     */
    Object sPop(String key);

    /**
     * 随机弹出多个元素
     *
     * @param key 键
     * @param count 数量
     * @return 值列表
     */
    List<Object> sPop(String key, long count);

    /**
     * 将元素从source集合移动到destination集合
     *
     * @param sourceKey 源键
     * @param value 值
     * @param destinationKey 目标键
     * @return 是否成功
     */
    Boolean sMove(String sourceKey, Object value, String destinationKey);

    /**
     * 获取集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    Long sSize(String key);

    /**
     * 判断元素是否在集合中
     *
     * @param key 键
     * @param value 值
     * @return 是否存在
     */
    Boolean sIsMember(String key, Object value);

    /**
     * 获取集合中的所有元素
     *
     * @param key 键
     * @return 值集合
     */
    Set<Object> sMembers(String key);

    /**
     * 随机获取一个元素
     *
     * @param key 键
     * @return 值
     */
    Object sRandomMember(String key);

    /**
     * 随机获取多个元素
     *
     * @param key 键
     * @param count 数量
     * @return 值集合
     */
    List<Object> sRandomMembers(String key, long count);

    /**
     * 获取交集
     *
     * @param key 键
     * @param otherKey 其他键
     * @return 交集
     */
    Set<Object> sIntersect(String key, String otherKey);

    /**
     * 获取交集
     *
     * @param key 键
     * @param otherKeys 其他键集合
     * @return 交集
     */
    Set<Object> sIntersect(String key, Collection<String> otherKeys);

    /**
     * 获取并集
     *
     * @param key 键
     * @param otherKey 其他键
     * @return 并集
     */
    Set<Object> sUnion(String key, String otherKey);

    /**
     * 获取并集
     *
     * @param key 键
     * @param otherKeys 其他键集合
     * @return 并集
     */
    Set<Object> sUnion(String key, Collection<String> otherKeys);

    /**
     * 获取差集
     *
     * @param key 键
     * @param otherKey 其他键
     * @return 差集
     */
    Set<Object> sDifference(String key, String otherKey);

    /**
     * 获取差集
     *
     * @param key 键
     * @param otherKeys 其他键集合
     * @return 差集
     */
    Set<Object> sDifference(String key, Collection<String> otherKeys);

    // ------------------ 有序集合操作 ------------------

    /**
     * 添加元素
     *
     * @param key 键
     * @param value 值
     * @param score 分数
     * @return 是否成功
     */
    Boolean zAdd(String key, Object value, double score);

    /**
     * 批量添加元素
     *
     * @param key 键
     * @param tuples 元素分数元组集合
     * @return 添加的元素数量
     */
    Long zAdd(String key, Set<TypedTuple<Object>> tuples);

    /**
     * 删除元素
     *
     * @param key 键
     * @param values 值数组
     * @return 删除的元素数量
     */
    Long zRemove(String key, Object... values);

    /**
     * 按分数递增获取排名
     *
     * @param key 键
     * @param value 值
     * @return 排名
     */
    Long zRank(String key, Object value);

    /**
     * 按分数递减获取排名
     *
     * @param key 键
     * @param value 值
     * @return 排名
     */
    Long zReverseRank(String key, Object value);

    /**
     * 按索引区间获取元素（分数从低到高）
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 值集合
     */
    Set<Object> zRange(String key, long start, long end);

    /**
     * 按索引区间获取元素（分数从高到低）
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 值集合
     */
    Set<Object> zReverseRange(String key, long start, long end);

    /**
     * 按索引区间获取元素和分数（分数从低到高）
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 元素分数元组集合
     */
    Set<TypedTuple<Object>> zRangeWithScores(String key, long start, long end);

    /**
     * 按索引区间获取元素和分数（分数从高到低）
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 元素分数元组集合
     */
    Set<TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end);

    /**
     * 按分数区间获取元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 值集合
     */
    Set<Object> zRangeByScore(String key, double min, double max);

    /**
     * 按分数区间获取元素（分数从高到低）
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 值集合
     */
    Set<Object> zReverseRangeByScore(String key, double min, double max);

    /**
     * 按分数区间获取元素和分数
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素分数元组集合
     */
    Set<TypedTuple<Object>> zRangeByScoreWithScores(String key, double min, double max);

    /**
     * 按分数区间获取元素和分数（分数从高到低）
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素分数元组集合
     */
    Set<TypedTuple<Object>> zReverseRangeByScoreWithScores(String key, double min, double max);

    /**
     * 统计分数区间的元素数量
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素数量
     */
    Long zCount(String key, double min, double max);

    /**
     * 获取有序集合大小
     *
     * @param key 键
     * @return 有序集合大小
     */
    Long zSize(String key);

    /**
     * 获取元素分数
     *
     * @param key 键
     * @param value 值
     * @return 分数
     */
    Double zScore(String key, Object value);

    /**
     * 删除分数区间的元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 删除的元素数量
     */
    Long zRemoveRangeByScore(String key, double min, double max);

    /**
     * 删除索引区间的元素
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 删除的元素数量
     */
    Long zRemoveRange(String key, long start, long end);

    /**
     * 增加元素分数
     *
     * @param key 键
     * @param value 值
     * @param delta 增量
     * @return 增加后的分数
     */
    Double zIncrementScore(String key, Object value, double delta);
} 