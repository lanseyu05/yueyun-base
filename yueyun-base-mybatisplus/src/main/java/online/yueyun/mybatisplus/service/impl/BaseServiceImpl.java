package online.yueyun.mybatisplus.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mybatisplus.service.BaseService;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础服务实现类，实现通用增强功能
 *
 * @param <M> Mapper类型
 * @param <T> 实体类型
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

    @Override
    public boolean saveSafely(T entity) {
        // 在保存前进行字段校验
        validateEntity(entity);
        return save(entity);
    }

    @Override
    public boolean updateSafely(T entity) {
        // 在更新前进行字段校验
        validateEntity(entity);
        return updateById(entity);
    }

    @Override
    public boolean removeSafely(Serializable id) {
        // 在删除前检查权限
        checkPermission(id);
        return removeById(id);
    }

    @Override
    public List<T> listByIdsOrdered(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<>();
        }

        // 查询所有实体
        List<T> entities = listByIds(idList);
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }

        // 创建ID到实体的映射
        Map<Serializable, T> idEntityMap = new HashMap<>();
        for (T entity : entities) {
            idEntityMap.put(getEntityId(entity), entity);
        }

        // 按照原始ID列表的顺序返回实体
        return idList.stream()
                .map(idEntityMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<T> selectPage(IPage<T> page, Wrapper<T> wrapper) {
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public T getOne(Wrapper<T> wrapper) {
        // 重写getOne方法，当查询结果多于一条时抛出异常
        List<T> list = list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (list.size() > 1) {
            throw new RuntimeException("查询结果包含多条记录");
        }
        return list.get(0);
    }

    @Override
    public T getByIdOrFail(Serializable id) {
        T entity = getById(id);
        if (entity == null) {
            throw new RuntimeException("未找到对应记录，ID: " + id);
        }
        return entity;
    }

    @Override
    public T getOneOrFail(Wrapper<T> wrapper) {
        T entity = getOne(wrapper);
        if (entity == null) {
            throw new RuntimeException("未找到对应记录");
        }
        return entity;
    }

    @Override
    public <K, V> Map<K, V> getMap(String column, String value) {
        return getMap(column, value, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getMap(String column, String value, Wrapper<T> wrapper) {
        Map<K, V> result = new HashMap<>();
        List<T> list = wrapper == null ? list() : list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        try {
            // 获取实体类
            Class<?> entityClass = list.get(0).getClass();
            
            // 获取字段对应的get方法
            String keyMethodName = "get" + column.substring(0, 1).toUpperCase() + column.substring(1);
            String valueMethodName = "get" + value.substring(0, 1).toUpperCase() + value.substring(1);
            
            for (T entity : list) {
                K key = (K) entityClass.getMethod(keyMethodName).invoke(entity);
                V val = (V) entityClass.getMethod(valueMethodName).invoke(entity);
                if (key != null) {
                    result.put(key, val);
                }
            }
        } catch (Exception e) {
            throw ExceptionUtils.mpe("获取Map失败: %s", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取实体ID
     *
     * @param entity 实体
     * @return ID
     */
    protected Serializable getEntityId(T entity) {
        try {
            // 尝试使用getById方法获取ID
            return (Serializable) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            log.warn("获取实体ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验实体字段
     * 子类可以重写此方法实现自定义校验逻辑
     *
     * @param entity 实体
     */
    protected void validateEntity(T entity) {
        // 默认不做任何校验，子类可以重写此方法
    }

    /**
     * 检查操作权限
     * 子类可以重写此方法实现自定义权限检查
     *
     * @param id 实体ID
     */
    protected void checkPermission(Serializable id) {
        // 默认不做任何权限检查，子类可以重写此方法
    }
}