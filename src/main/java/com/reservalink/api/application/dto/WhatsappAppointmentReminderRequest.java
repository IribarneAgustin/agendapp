package com.reservalink.api.application.dto;

import java.util.List;

public record WhatsappAppointmentReminderRequest(
        String messaging_product,
        String to,
        String type,
        Template template
) {

    public static WhatsappAppointmentReminderRequest of(
            String phone,
            String name,
            String serviceName,
            String date,
            String time,
            String bookingId
    ) {
        return new WhatsappAppointmentReminderRequest(
                "whatsapp",
                phone,
                "template",
                new Template(
                        "appointment_reminder",
                        new Language("es_AR"),
                        List.of(
                                Component.body(
                                        name,
                                        serviceName,
                                        date,
                                        time
                                ),
                                Component.button(bookingId)
                        )
                )
        );
    }

    public record Template(
            String name,
            Language language,
            List<Component> components
    ) {
    }

    public record Language(String code) {
    }

    public record Component(
            String type,
            String sub_type,
            String index,
            List<Parameter> parameters
    ) {

        static Component body(
                String name,
                String serviceName,
                String date,
                String time
        ) {
            return new Component(
                    "body",
                    null,
                    null,
                    List.of(
                            new Parameter("text", name),
                            new Parameter("text", serviceName),
                            new Parameter("text", date),
                            new Parameter("text", time)
                    )
            );
        }

        static Component button(String bookingId) {
            return new Component(
                    "button",
                    "url",
                    "0",
                    List.of(
                            new Parameter("text", bookingId)
                    )
            );
        }
    }

    public record Parameter(String type, String text) {
    }
}