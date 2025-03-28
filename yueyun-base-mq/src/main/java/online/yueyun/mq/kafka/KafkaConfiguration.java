package online.yueyun.mq.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import online.yueyun.mq.config.MqProperties;
import online.yueyun.mq.service.MessageConsumedService;
import online.yueyun.mq.service.MessageRecordService;
import online.yueyun.mq.service.MessageService;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "yueyun.mq.kafka", name = "enabled", havingValue = "true")
public class KafkaConfiguration {

    /**
     * 配置生产者工厂
     */
    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory<String, byte[]> kafkaProducerFactory(MqProperties properties) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        configs.put(ProducerConfig.RETRIES_CONFIG, properties.getKafka().getProducer().getRetries());
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.getKafka().getProducer().getBatchSize());
        configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, properties.getKafka().getProducer().getBufferMemory());
        return new DefaultKafkaProducerFactory<>(configs);
    }

    /**
     * 配置Kafka模板
     */
    @Bean
    @ConditionalOnMissingBean(KafkaTemplate.class)
    public KafkaTemplate<String, byte[]> kafkaTemplate(ProducerFactory<String, byte[]> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * 配置消费者工厂
     */
    @Bean
    @ConditionalOnMissingBean(ConsumerFactory.class)
    public ConsumerFactory<String, byte[]> kafkaConsumerFactory(MqProperties properties) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * 配置消费者容器工厂
     */
    @Bean
    @ConditionalOnMissingBean(ConcurrentKafkaListenerContainerFactory.class)
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /**
     * 配置消费者容器
     */
    @Bean
    @ConditionalOnMissingBean(ConcurrentMessageListenerContainer.class)
    public ConcurrentMessageListenerContainer<String, byte[]> kafkaListenerContainer(
            ConcurrentKafkaListenerContainerFactory<String, byte[]> factory,
            MqProperties properties) {
        ContainerProperties containerProperties = new ContainerProperties(properties.getDefaultTopic());
        containerProperties.setGroupId(properties.getDefaultGroup());
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        ConcurrentMessageListenerContainer<String, byte[]> container = 
            (ConcurrentMessageListenerContainer<String, byte[]>) factory.createContainer(properties.getDefaultTopic());
        container.setAutoStartup(false);
        return container;
    }

    /**
     * 配置消息服务
     */
    @Bean
    @ConditionalOnMissingBean(MessageService.class)
    public MessageService kafkaMessageService(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            ConcurrentMessageListenerContainer<String, byte[]> container,
            MqProperties properties,
            MessageRecordService messageRecordService,
            MessageConsumedService messageConsumedService) {
        log.info("初始化Kafka消息服务: bootstrapServers={}", properties.getKafka().getBootstrapServers());
        return new KafkaMessageServiceImpl(
                kafkaTemplate,
                container,
                properties,
                messageRecordService,
                messageConsumedService);
    }
} 