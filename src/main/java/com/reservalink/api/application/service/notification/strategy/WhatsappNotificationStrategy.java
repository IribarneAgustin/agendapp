package com.reservalink.api.application.service.notification.strategy;

import com.reservalink.api.application.dto.WhatsappAppointmentReminderRequest;
import com.reservalink.api.application.output.WhatsAppClientPort;
import com.reservalink.api.application.service.notification.NotificationChannel;
import com.reservalink.api.application.service.notification.NotificationMotive;
import com.reservalink.api.application.service.notification.NotificationTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class WhatsappNotificationStrategy implements NotificationStrategy {

    private final WhatsAppClientPort whatsAppClientPort;

    public WhatsappNotificationStrategy(WhatsAppClientPort whatsAppClientPort) {
        this.whatsAppClientPort = whatsAppClientPort;
    }

    @Override
    public NotificationChannel getType() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public String getTemplatePath() {
        return "templates/whatsapp/";
    }

    @Override
    public void send(NotificationTarget target,
                     NotificationMotive motive,
                     Map<String, String> args) {

        String phone = target.getPhone();
        if (phone == null) {
            throw new IllegalArgumentException("Phone number required for WhatsApp notification");
        }

        log.info("Sending whatsapp notification for booking {}", args.get("bookingId"));

        WhatsappAppointmentReminderRequest request =
                WhatsappAppointmentReminderRequest.of(
                        phone,
                        args.get("name"),
                        args.get("serviceName"),
                        args.get("date"),
                        args.get("time"),
                        args.get("bookingId")
                );

        whatsAppClientPort.sendMessage(request);

        log.info("Whatsapp notification sent for booking {}", args.get("bookingId"));
    }
}