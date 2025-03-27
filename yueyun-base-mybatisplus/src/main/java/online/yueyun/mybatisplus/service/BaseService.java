package online.yueyun.mybatisplus.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基础服务接口，扩展MyBatis-Plus的IService
 *
 * @param <T> 实体类型
 * @author YueYun
 * @since 1.0.0
 */
public interface BaseService<T> extends IService<T> {

    /**
     * 安全保存，会检查字段合法性
     *
     * @param entity 实体
     * @return 是否成功
     */
    boolean saveSafely(T entity);

    /**
     * 安全更新，会检查字段合法性
     *
     * @param entity 实体
     * @return 是否成功
     */
    boolean updateSafely(T entity);

    /**
     * 安全删除，会检查权限
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean removeSafely(Serializable id);

    /**
     * 按ID批量查询，结果按给定的ID顺序排序
     *
     * @param idList ID列表
     * @return 实体列表，按ID顺序排序
     */
    List<T> listByIdsOrdered(Collection<? extends Serializable> idList);

    /**
     * 分页查询，支持动态条件
     *
     * @param page    分页参数
     * @param wrapper 条件构造器
     * @return 分页结果
     */
    IPage<T> selectPage(IPage<T> page, Wrapper<T> wrapper);

    /**
     * 获取单条记录，如果存在多条则抛出异常
     *
     * @param wrapper 条件构造器
     * @return 单条记录
     */
    T getOne(Wrapper<T> wrapper);

    /**
     * 获取单条记录，如果不存在则抛出异常
     *
     * @param id 主键
     * @return 单条记录
     */
    T getByIdOrFail(Serializable id);

    /**
     * 获取单条记录，如果不存在则抛出异常
     *
     * @param wrapper 条件构造器
     * @return 单条记录
     */
    T getOneOrFail(Wrapper<T> wrapper);

    /**
     * 获取映射，将指定的字段映射为键值对
     *
     * @param column 键字段
     * @param value  值字段
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 映射结果
     */
    <K, V> Map<K, V> getMap(String column, String value);

    /**
     * 获取映射，将指定的字段映射为键值对
     *
     * @param column  键字段
     * @param value   值字段
     * @param wrapper 条件构造器
     * @param <K>     键类型
     * @param <V>     值类型
     * @return 映射结果
     */
    <K, V> Map<K, V> getMap(String column, String value, Wrapper<T> wrapper);
} 