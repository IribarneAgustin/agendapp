package com.reservalink.api.service.notification;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface NotificationStrategy {

    NotificationType getType();

    String getTemplatePath();

    @Async
    void send(String receiver, NotificationMotive motive, Map<String, String> args);
}
