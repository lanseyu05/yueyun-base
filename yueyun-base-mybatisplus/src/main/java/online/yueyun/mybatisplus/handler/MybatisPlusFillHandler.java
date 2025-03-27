package online.yueyun.mybatisplus.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mybatisplus.config.MybatisPlusProperties;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MyBatis-Plus字段自动填充处理器
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class MybatisPlusFillHandler implements MetaObjectHandler {

    private final MybatisPlusProperties properties;

    @Override
    public void insertFill(MetaObject metaObject) {
        if (!properties.getFieldFill().isEnabled()) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            String currentUserId = getCurrentUserId();

            // 创建时间
            String createTimeField = properties.getFieldFill().getCreateTimeField();
            if (StringUtils.hasText(createTimeField) && metaObject.hasSetter(createTimeField)) {
                Object currentCreateTime = getFieldValByName(createTimeField, metaObject);
                if (currentCreateTime == null) {
                    setFieldValByName(createTimeField, now, metaObject);
                }
            }

            // 创建人
            String createUserField = properties.getFieldFill().getCreateUserField();
            if (StringUtils.hasText(createUserField) && StringUtils.hasText(currentUserId) && metaObject.hasSetter(createUserField)) {
                Object currentCreateUser = getFieldValByName(createUserField, metaObject);
                if (currentCreateUser == null) {
                    setFieldValByName(createUserField, currentUserId, metaObject);
                }
            }

            // 更新时间
            String updateTimeField = properties.getFieldFill().getUpdateTimeField();
            if (StringUtils.hasText(updateTimeField) && metaObject.hasSetter(updateTimeField)) {
                Object currentUpdateTime = getFieldValByName(updateTimeField, metaObject);
                if (currentUpdateTime == null) {
                    setFieldValByName(updateTimeField, now, metaObject);
                }
            }

            // 更新人
            String updateUserField = properties.getFieldFill().getUpdateUserField();
            if (StringUtils.hasText(updateUserField) && StringUtils.hasText(currentUserId) && metaObject.hasSetter(updateUserField)) {
                Object currentUpdateUser = getFieldValByName(updateUserField, metaObject);
                if (currentUpdateUser == null) {
                    setFieldValByName(updateUserField, currentUserId, metaObject);
                }
            }
        } catch (Exception e) {
            log.error("MyBatis-Plus 字段填充异常", e);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (!properties.getFieldFill().isEnabled()) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            String currentUserId = getCurrentUserId();

            // 更新时间
            String updateTimeField = properties.getFieldFill().getUpdateTimeField();
            if (StringUtils.hasText(updateTimeField) && metaObject.hasSetter(updateTimeField)) {
                setFieldValByName(updateTimeField, now, metaObject);
            }

            // 更新人
            String updateUserField = properties.getFieldFill().getUpdateUserField();
            if (StringUtils.hasText(updateUserField) && StringUtils.hasText(currentUserId) && metaObject.hasSetter(updateUserField)) {
                setFieldValByName(updateUserField, currentUserId, metaObject);
            }
        } catch (Exception e) {
            log.error("MyBatis-Plus 字段填充异常", e);
        }
    }

    /**
     * 获取当前用户ID
     * 默认从请求头中获取，可以由具体项目重写
     *
     * @return 当前用户ID
     */
    protected String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (Objects.nonNull(attributes)) {
                HttpServletRequest request = attributes.getRequest();
                // 默认从请求头中获取用户ID，具体项目可以自定义
                String userId = request.getHeader("X-User-Id");
                if (StringUtils.hasText(userId)) {
                    return userId;
                }
            }
        } catch (Exception e) {
            log.debug("获取当前用户ID失败", e);
        }
        return null;
    }
} 