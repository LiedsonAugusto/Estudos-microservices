package com.estudo.schedulingService.schedulers;

import com.estudo.schedulingService.entities.Appointment;
import com.estudo.schedulingService.enums.AppointmentStatus;
import com.estudo.schedulingService.producers.AppointmentProducer;
import com.estudo.schedulingService.repositories.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class AppointmentReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentProducer appointmentProducer;

    public AppointmentReminderScheduler(AppointmentRepository appointmentRepository,
                                       AppointmentProducer appointmentProducer) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentProducer = appointmentProducer;
    }

    // @Scheduled define QUANDO esta tarefa vai executar
    // cron = "0 0 9 * * ?" → Executa todo dia às 09:00
    // Formato cron: segundo minuto hora dia mês dia-da-semana
    @Scheduled(cron = "0 0 9 * * ?") // Todo dia às 09:00
    public void sendDailyReminders() {
        log.info("Iniciando envio de lembretes diários...");

        // buscar appointments para amanhã (24h de antecedência)
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getTimeSlot().getDate().equals(tomorrow))
                .filter(apt -> apt.getStatus() == AppointmentStatus.SCHEDULED
                            || apt.getStatus() == AppointmentStatus.CONFIRMED)
                .toList();

        log.info("Encontrados {} agendamentos para enviar lembrete", appointments.size());

        for (Appointment appointment : appointments) {
            try {
                appointmentProducer.publishAppointmentReminder(appointment);
                log.info("Lembrete enviado para appointment {}", appointment.getId());
            } catch (Exception e) {
                log.error("Erro ao enviar lembrete para appointment {}: {}",
                         appointment.getId(), e.getMessage());
            }
        }

        log.info("Envio de lembretes concluído!");
    }
}
