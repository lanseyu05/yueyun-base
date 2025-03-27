package online.yueyun.excel.config;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.excel.service.ExcelService;
import online.yueyun.excel.service.impl.ExcelServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Excel自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ExcelProperties.class)
@ConditionalOnProperty(prefix = "yueyun.excel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExcelAutoConfiguration {

    /**
     * 配置Excel服务
     *
     * @param properties 配置属性
     * @return Excel服务
     */
    @Bean
    @ConditionalOnMissingBean
    public ExcelService excelService(ExcelProperties properties) {
        log.info("初始化Excel服务");
        return new ExcelServiceImpl(properties);
    }
}