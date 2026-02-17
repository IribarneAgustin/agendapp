package com.reservalink.api.application.output;

import com.reservalink.api.application.dto.WhatsappAppointmentReminderRequest;

public interface WhatsAppClientPort {
    void sendMessage(WhatsappAppointmentReminderRequest request);
}