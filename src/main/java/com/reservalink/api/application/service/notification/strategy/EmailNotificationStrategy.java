package com.reservalink.api.application.service.notification.strategy;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.reservalink.api.application.service.notification.NotificationChannel;
import com.reservalink.api.application.service.notification.NotificationMotive;
import com.reservalink.api.application.service.notification.NotificationTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.util.Map;

@Slf4j
@Service
public class EmailNotificationStrategy implements NotificationStrategy {

    private final MustacheFactory mustacheFactory;
    private final RestTemplate restTemplate;
    private final String domain;
    private final String apiKey;

    @Value("${mailgun.api.email-from}")
    private String emailFrom;

    public EmailNotificationStrategy(
            MustacheFactory mustacheFactory,
            @Value("${mailgun.api.key}") String mailgunApiKey,
            @Value("${mailgun.api.domain}") String domain
    ) {
        this.mustacheFactory = mustacheFactory;
        this.domain = domain;
        this.apiKey = mailgunApiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public NotificationChannel getType() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public String getTemplatePath() {
        return "templates/email/";
    }

    @Override
    @Async
    public void send(NotificationTarget target, NotificationMotive motive, Map<String, String> args) {
        try {
            String email = target.getEmail();
            if (email == null) {
                throw new IllegalArgumentException("Email required");
            }
            String templatePath = getTemplatePath() + motive.getTemplateName() + ".mustache";
            Mustache mustache = mustacheFactory.compile(templatePath);
            StringWriter writer = new StringWriter();
            mustache.execute(writer, args).flush();

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("api", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("from", "ReservaLink <" + emailFrom + ">");
            body.add("to", email);
            body.add("subject", motive.getSubject());
            body.add("html", writer.toString());

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    "https://api.mailgun.net/v3/" + domain + "/messages",
                    request,
                    String.class
            );

            log.info("Email sent via Mailgun to {}", email);

        } catch (Exception e) {
            log.error("Unexpected error sending email", e);
            throw new RuntimeException(
                    "Error sending email notification for motive " + motive.name(), e
            );
        }
    }
}
