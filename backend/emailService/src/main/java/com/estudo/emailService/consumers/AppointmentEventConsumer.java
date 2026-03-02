package com.estudo.emailService.consumers;

import com.estudo.emailService.dtos.AppointmentCancelledEvent;
import com.estudo.emailService.dtos.AppointmentCreatedEvent;
import com.estudo.emailService.dtos.AppointmentReminderEvent;
import com.estudo.emailService.exceptions.EmailSendingException;
import com.estudo.emailService.exceptions.InvalidEventDataException;
import com.estudo.emailService.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventConsumer.class);

    private final EmailService emailService;

    public AppointmentEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    // 3 metodos escutando a mesma queue, o spring difere qual chamar pelo o parametro que o metodo espera

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("============================================");
        log.info("📧 EVENTO RECEBIDO: Agendamento Criado");
        log.info("============================================");
        log.info("Appointment ID: {}", event.appointmentId());
        log.info("Usuário: {}", event.userName());
        log.info("Email: {}", event.userEmail());
        log.info("Serviço: {}", event.serviceName());
        log.info("Data: {}", event.date());
        log.info("Horário: {}", event.time());
        log.info("Código: {}", event.confirmationCode());
        log.info("============================================");

        try {
            emailService.sendEmailToAppointmentCreated(event);
            log.info("✅ Evento processado com sucesso!");
        } catch (InvalidEventDataException e) {
            log.error("❌ DADOS INVÁLIDOS no evento AppointmentCreated [id={}]: {}", event.appointmentId(), e.getMessage());
        } catch (EmailSendingException e) {
            log.error("❌ FALHA NO ENVIO do email para {} [id={}]: {}", event.userEmail(), event.appointmentId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        log.info("============================================");
        log.info("📧 EVENTO RECEBIDO: Agendamento cancelado");
        log.info("============================================");
        log.info("Appointment ID: {}", event.appointmentId());
        log.info("Usuário: {}", event.userName());
        log.info("Email: {}", event.userEmail());
        log.info("Serviço: {}", event.serviceName());
        log.info("Data: {}", event.date());
        log.info("Horário: {}", event.time());
        log.info("Razão: {}", event.reason());
        log.info("============================================");

        try {
            emailService.sendEmailToAppointmentCancelled(event);
            log.info("✅ Evento processado com sucesso!");
        } catch (InvalidEventDataException e) {
            log.error("❌ DADOS INVÁLIDOS no evento AppointmentCancelled [id={}]: {}", event.appointmentId(), e.getMessage());
        } catch (EmailSendingException e) {
            log.error("❌ FALHA NO ENVIO do email para {} [id={}]: {}", event.userEmail(), event.appointmentId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleAppointmentReminder(AppointmentReminderEvent event) {
        log.info("============================================");
        log.info("📧 EVENTO RECEBIDO: Relembrando agendamento");
        log.info("============================================");
        log.info("Appointment ID: {}", event.appointmentId());
        log.info("Usuário: {}", event.userName());
        log.info("Email: {}", event.userEmail());
        log.info("Serviço: {}", event.serviceName());
        log.info("Data: {}", event.date());
        log.info("Horário: {}", event.time());
        log.info("Código: {}", event.confirmationCode());
        log.info("============================================");

        try {
            emailService.sendEmailToAppointmentReminder(event);
            log.info("✅ Evento processado com sucesso!");
        } catch (InvalidEventDataException e) {
            log.error("❌ DADOS INVÁLIDOS no evento AppointmentReminder [id={}]: {}", event.appointmentId(), e.getMessage());
        } catch (EmailSendingException e) {
            log.error("❌ FALHA NO ENVIO do email para {} [id={}]: {}", event.userEmail(), event.appointmentId(), e.getMessage());
            throw e;
        }
    }
}
