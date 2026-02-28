package com.reservalink.api.adapter.output.providers;

import com.reservalink.api.application.dto.WhatsappAppointmentReminderRequest;
import com.reservalink.api.application.output.WhatsAppClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class MetaClientAdapter implements WhatsAppClientPort {

    private final RestTemplate restTemplate;
    private final String url;
    private final String token;

    public MetaClientAdapter(@Value("${meta.whatsapp.number-id}") String numberId,
                             @Value("${meta.whatsapp.token}") String token,
                             RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
        this.url = "https://graph.facebook.com/v22.0/" + numberId + "/messages";
        this.token = token;
    }

    @Override
    public void sendMessage(WhatsappAppointmentReminderRequest payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<WhatsappAppointmentReminderRequest> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("WhatsApp message sent successfully: {}", response.getBody());

        } catch (Exception e) {
            log.error("Error sending WhatsApp message to Meta API", e);
            throw e;
        }
    }

}