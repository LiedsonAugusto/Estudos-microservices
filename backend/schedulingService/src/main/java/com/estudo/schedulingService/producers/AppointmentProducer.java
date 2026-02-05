package com.estudo.schedulingService.producers;

import com.estudo.schedulingService.dtos.AppointmentCancelledEvent;
import com.estudo.schedulingService.dtos.AppointmentCreatedEvent;
import com.estudo.schedulingService.dtos.AppointmentReminderEvent;
import com.estudo.schedulingService.entities.Appointment;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppointmentProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.scheduling}")
    private String schedulingExchange;

    @Value("${rabbitmq.routingkey.appointment.created}")
    private String appointmentCreatedRoutingKey;

    @Value("${rabbitmq.routingkey.appointment.cancelled}")
    private String appointmentCancelledRoutingKey;

    @Value("${rabbitmq.routingkey.appointment.reminder}")
    private String appointmentReminderRoutingKey;

    public AppointmentProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Método 1 - Publicar evento de criação
    public void publishAppointmentCreated(Appointment appointment) {

        var appointmentCreatedEvent = AppointmentCreatedEvent.from(
                appointment.getId(),
                appointment.getUserEmail(),
                appointment.getUserName(),
                appointment.getTimeSlot().getService().getName(),
                appointment.getTimeSlot().getDate(),
                appointment.getTimeSlot().getStartTime(),
                appointment.getConfirmationCode()
        );

        rabbitTemplate.convertAndSend(schedulingExchange, appointmentCreatedRoutingKey, appointmentCreatedEvent);
    }

    // Método 2 - Publicar evento de cancelamento
    public void publishAppointmentCancelled(Appointment appointment, String reason) {
        var appointmentCancelledEvent = AppointmentCancelledEvent.from(
                appointment.getId(),
                appointment.getUserEmail(),
                appointment.getUserName(),
                appointment.getTimeSlot().getService().getName(),
                appointment.getTimeSlot().getDate(),
                appointment.getTimeSlot().getStartTime(),
                reason
        );

        rabbitTemplate.convertAndSend(schedulingExchange, appointmentCancelledRoutingKey, appointmentCancelledEvent);
    }

    // Método 3 - Publicar evento de lembrete
    public void publishAppointmentReminder(Appointment appointment) {

        var appointmentReminderEvent = AppointmentReminderEvent.from(
                appointment.getId(),
                appointment.getUserEmail(),
                appointment.getUserName(),
                appointment.getTimeSlot().getService().getName(),
                appointment.getTimeSlot().getDate(),
                appointment.getTimeSlot().getStartTime(),
                appointment.getConfirmationCode()
        );

        rabbitTemplate.convertAndSend(schedulingExchange, appointmentReminderRoutingKey, appointmentReminderEvent);
    }


}
