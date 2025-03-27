package online.yueyun.skywalking.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.skywalking.annotation.Trace;
import online.yueyun.skywalking.config.TracingProperties;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 链路追踪切面
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Aspect
public class TracingAspect {

    private final TracingProperties properties;
    private final ObjectMapper objectMapper;

    public TracingAspect(TracingProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 定义切点
     */
    @Pointcut("@annotation(online.yueyun.skywalking.annotation.Trace) || @within(online.yueyun.skywalking.annotation.Trace)")
    public void tracePointcut() {
    }

    /**
     * 环绕通知
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 异常信息
     */
    @Around("tracePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取类和方法上的注解
        Trace trace = AnnotationUtils.findAnnotation(method, Trace.class);
        if (trace == null) {
            trace = AnnotationUtils.findAnnotation(method.getDeclaringClass(), Trace.class);
        }
        
        // 操作名称
        String operationName = trace.operationName();
        if (!StringUtils.hasText(operationName)) {
            operationName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
        
        // 获取追踪ID
        String traceId = TraceContext.traceId();
        if (StringUtils.hasText(traceId)) {
            log.debug("链路追踪ID: {}", traceId);
            ActiveSpan.tag("traceId", traceId);
        }
        
        // 记录方法开始执行
        ActiveSpan.info("开始执行: " + operationName);
        
        // 是否记录参数
        boolean logParameters = properties.isLogParameters() && trace.logParameters();
        if (logParameters) {
            try {
                // 提取参数
                String[] paramNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();
                Map<String, Object> params = new HashMap<>(paramNames.length);
                for (int i = 0; i < paramNames.length; i++) {
                    params.put(paramNames[i], args[i]);
                }
                
                // 转换为JSON
                String paramsJson = objectMapper.writeValueAsString(params);
                if (paramsJson.length() > properties.getMaxLogLength()) {
                    paramsJson = paramsJson.substring(0, properties.getMaxLogLength()) + "...";
                }
                
                // 记录参数
                ActiveSpan.info("方法参数: " + paramsJson);
            } catch (Exception e) {
                log.warn("记录方法参数异常", e);
            }
        }
        
        // 执行方法
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            
            // 是否记录结果
            boolean logResult = properties.isLogResult() && trace.logResult();
            if (logResult && result != null) {
                try {
                    // 转换为JSON
                    String resultJson = objectMapper.writeValueAsString(result);
                    if (resultJson.length() > properties.getMaxLogLength()) {
                        resultJson = resultJson.substring(0, properties.getMaxLogLength()) + "...";
                    }
                    
                    // 记录结果
                    ActiveSpan.info("方法结果: " + resultJson);
                } catch (Exception e) {
                    log.warn("记录方法结果异常", e);
                }
            }
            
            return result;
        } catch (Throwable e) {
            // 是否记录异常
            boolean logException = properties.isLogException() && trace.logException();
            if (logException) {
                ActiveSpan.error("执行异常: " + e.getMessage());
            }
            throw e;
        } finally {
            // 记录方法执行耗时
            long executeTime = System.currentTimeMillis() - startTime;
            ActiveSpan.info("执行完成: " + operationName + ", 耗时: " + executeTime + "ms");
        }
    }
} 