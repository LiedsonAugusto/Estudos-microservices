package com.estudo.emailService.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.user}")
    private String userExchange;

    @Value("${rabbitmq.routingkey.user.created}")
    private String userCreatedRoutingKey;

    @Value("${rabbitmq.queue.email}")
    private String emailQueue;

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue, true, false, false);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(userExchange);
    }

    @Bean
    public Binding bindingUserCreatedToEmailQueue(Queue emailQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(emailQueue)                    // Pega a queue
                .to(userExchange)                    // Liga ao exchange
                .with(userCreatedRoutingKey);        // Usando esta routing key
    }
}
