package com.estudo.schedulingService.configs;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.scheduling}")
    private String schedulingExchange;

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange schedulingExchange() {
        return new TopicExchange(schedulingExchange, true, false);
        // - nome: schedulingExchange (da property)
        // - durable: true (sobrevive a restart)
        // - autoDelete: false (não deleta automaticamente)
    }
}
