package com.estudo.emailService.services;

import com.estudo.emailService.dtos.UserCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String sender;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailToUserCreated(UserCreatedEvent userCreatedEvent) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(userCreatedEvent.email());
            message.setSubject("Bem-vindo ao Sistema de Agendamentos!");
            message.setText("Olá " + userCreatedEvent.name() + ",\n\n" +
                    "Bem-vindo ao nosso sistema de agendamentos!\n\n" +
                    "Agora você pode agendar seus atendimentos de forma rápida e prática.\n\n" +
                    "Atenciosamente,\n" +
                    "Equipe de Agendamentos");
            mailSender.send(message);


    }
}
