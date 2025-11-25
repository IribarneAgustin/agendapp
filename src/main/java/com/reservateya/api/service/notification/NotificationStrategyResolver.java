package com.reservateya.api.service.notification;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Component
public class NotificationStrategyResolver {

    private final Map<NotificationType, NotificationStrategy> strategies;

    public NotificationStrategyResolver(List<NotificationStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(NotificationStrategy::getType, s -> s));
    }

    public NotificationStrategy resolve(NotificationType type) {
        NotificationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported notification type: " + type);
        }
        return strategy;
    }
}
