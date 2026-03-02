package com.estudo.emailService.consumers;

import com.estudo.emailService.dtos.UserCreatedEvent;
import com.estudo.emailService.exceptions.EmailSendingException;
import com.estudo.emailService.exceptions.InvalidEventDataException;
import com.estudo.emailService.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final EmailService emailService;

    public UserEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("============================================");
        log.info("📧 EVENTO RECEBIDO: Usuário Criado");
        log.info("============================================");
        log.info("User ID: {}", event.userId());
        log.info("Nome: {}", event.name());
        log.info("Email: {}", event.email());
        log.info("Timestamp: {}", event.timestamp());
        log.info("============================================");

        try {
            emailService.sendEmailToUserCreated(event);
            log.info("✅ Evento processado com sucesso!");
        } catch (InvalidEventDataException e) {
            log.error("❌ DADOS INVÁLIDOS no evento UserCreated [id={}]: {}", event.userId(), e.getMessage());
        } catch (EmailSendingException e) {
            log.error("❌ FALHA NO ENVIO do email para {} [id={}]: {}", event.email(), event.userId(), e.getMessage());
            throw e;
        }
    }
}
