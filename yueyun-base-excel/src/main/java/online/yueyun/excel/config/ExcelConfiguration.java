package online.yueyun.excel.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.excel.service.EasyExcelService;
import online.yueyun.excel.service.impl.EasyExcelServiceImpl;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Excel配置类
 * 用于处理@EnableExcel注解的配置逻辑
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ExcelProperties.class)
@ConditionalOnProperty(prefix = "yueyun.excel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExcelConfiguration implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // 检查是否已经注册了ExcelProperties
        if (!registry.containsBeanDefinition("excelProperties")) {
            log.info("注册Excel配置属性");
            // 注册ExcelProperties
            registry.registerBeanDefinition("excelProperties",
                    BeanDefinitionBuilder
                            .genericBeanDefinition(ExcelProperties.class)
                            .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR)
                            .getBeanDefinition());
        }
    }

    /**
     * 配置Excel服务
     *
     * @param properties 配置属性
     * @return Excel服务
     */
    @Bean(name = "excelService")
    @ConditionalOnMissingBean(name = "excelService")
    public EasyExcelService excelService(ExcelProperties properties) {
        log.info("初始化Excel服务");
        return new EasyExcelServiceImpl(properties);
    }
} 