package com.estudo.emailService.services;

import com.estudo.emailService.dtos.AppointmentCancelledEvent;
import com.estudo.emailService.dtos.AppointmentCreatedEvent;
import com.estudo.emailService.dtos.AppointmentReminderEvent;
import com.estudo.emailService.dtos.UserCreatedEvent;
import com.estudo.emailService.exceptions.EmailSendingException;
import com.estudo.emailService.exceptions.InvalidEmailAddressException;
import com.estudo.emailService.exceptions.InvalidEventDataException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @Value("${spring.mail.username}")
    private String sender;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailToUserCreated(UserCreatedEvent event) {
        if (event.name() == null || event.name().isBlank()) {
            throw new InvalidEventDataException("Nome do usuário ausente no evento de criação de usuário");
        }
        validateEmail(event.email());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(event.email());
        message.setSubject("Bem-vindo ao Sistema de Agendamentos!");
        message.setText("Olá " + event.name() + ",\n\n" +
                "Bem-vindo ao nosso sistema de agendamentos!\n\n" +
                "Agora você pode agendar seus atendimentos de forma rápida e prática.\n\n" +
                "Atenciosamente,\n" +
                "Equipe de Agendamentos");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendingException(event.email(), "UserCreated", e);
        }
    }

    public void sendEmailToAppointmentCreated(AppointmentCreatedEvent event) {
        if (event.userName() == null || event.userName().isBlank()) {
            throw new InvalidEventDataException("Nome do usuário ausente no evento de criação de agendamento");
        }
        validateEmail(event.userEmail());

        String dataFormatada = event.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaFormatada = event.time().format(DateTimeFormatter.ofPattern("HH:mm"));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(event.userEmail());
        message.setSubject("Agendamento Confirmado - " + event.serviceName());
        message.setText(
                "Olá " + event.userName() + ",\n\n" +
                        "Seu agendamento foi confirmado com sucesso!\n\n" +
                        "Detalhes do Agendamento:\n" +
                        "- Serviço: " + event.serviceName() + "\n" +
                        "- Data: " + dataFormatada + "\n" +
                        "- Horário: " + horaFormatada + "\n" +
                        "- Código de Confirmação: " + event.confirmationCode() + "\n\n" +
                        "IMPORTANTE: Guarde este código! Você precisará dele no dia do atendimento.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe de Agendamentos"
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendingException(event.userEmail(), "AppointmentCreated", e);
        }
    }

    public void sendEmailToAppointmentCancelled(AppointmentCancelledEvent event) {
        if (event.userName() == null || event.userName().isBlank()) {
            throw new InvalidEventDataException("Nome do usuário ausente no evento de cancelamento de agendamento");
        }
        validateEmail(event.userEmail());

        String dataFormatada = event.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaFormatada = event.time().format(DateTimeFormatter.ofPattern("HH:mm"));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(event.userEmail());
        message.setSubject("Agendamento Cancelado - " + event.serviceName());
        message.setText(
                "Olá " + event.userName() + ",\n\n" +
                        "Seu agendamento foi cancelado.\n\n" +
                        "Detalhes do Agendamento Cancelado:\n" +
                        "- Serviço: " + event.serviceName() + "\n" +
                        "- Data: " + dataFormatada + "\n" +
                        "- Horário: " + horaFormatada + "\n" +
                        "- Motivo: " + event.reason() + "\n\n" +
                        "Você pode fazer um novo agendamento quando desejar.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe de Agendamentos"
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendingException(event.userEmail(), "AppointmentCancelled", e);
        }
    }

    public void sendEmailToAppointmentReminder(AppointmentReminderEvent event) {
        if (event.userName() == null || event.userName().isBlank()) {
            throw new InvalidEventDataException("Nome do usuário ausente no evento de lembrete de agendamento");
        }
        validateEmail(event.userEmail());

        String dataFormatada = event.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaFormatada = event.time().format(DateTimeFormatter.ofPattern("HH:mm"));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(event.userEmail());
        message.setSubject("Lembrete: Seu agendamento é amanhã!");
        message.setText(
                "Olá " + event.userName() + ",\n\n" +
                        "Lembramos que você tem um atendimento agendado para AMANHÃ!\n\n" +
                        "Detalhes:\n" +
                        "- Serviço: " + event.serviceName() + "\n" +
                        "- Data: " + dataFormatada + "\n" +
                        "- Horário: " + horaFormatada + "\n" +
                        "- Código de Confirmação: " + event.confirmationCode() + "\n\n" +
                        "Não esqueça de levar seus documentos e chegar com 10 minutos de antecedência.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe de Agendamentos"
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendingException(event.userEmail(), "AppointmentReminder", e);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEventDataException("Email do destinatário está ausente no evento");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new InvalidEmailAddressException(email);
        }
    }
}
