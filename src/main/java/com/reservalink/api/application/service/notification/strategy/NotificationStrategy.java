package com.reservalink.api.application.service.notification.strategy;

import com.reservalink.api.application.service.notification.NotificationChannel;
import com.reservalink.api.application.service.notification.NotificationMotive;
import com.reservalink.api.application.service.notification.NotificationTarget;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface NotificationStrategy {

    NotificationChannel getType();

    String getTemplatePath();

    CompletableFuture<Void> send(NotificationTarget target, NotificationMotive motive, Map<String, String> args);
}