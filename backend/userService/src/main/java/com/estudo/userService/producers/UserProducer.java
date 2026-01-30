package com.estudo.userService.producers;

import com.estudo.userService.dtos.UserCreatedEvent;
import com.estudo.userService.entities.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    private final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${rabbitmq.exchange.user}")
    private String userExchange;

    @Value("${rabbitmq.routingkey.user.created}")
    private String userCreatedRoutingKey;

    public void publishUserCreatedEvent(User user) {
        var event = UserCreatedEvent.from(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        rabbitTemplate.convertAndSend(userExchange, userCreatedRoutingKey, event);
    }

}
