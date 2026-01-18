package com.reservalink.api.service.notification;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
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
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    @Override
    public String getTemplatePath() {
        return "templates/email/";
    }

    @Override
    @Async
    public void send(String userEmail, NotificationMotive motive, Map<String, String> args) {
        try {
            String templatePath = getTemplatePath() + motive.getTemplateName() + ".mustache";
            Mustache mustache = mustacheFactory.compile(templatePath);
            StringWriter writer = new StringWriter();
            mustache.execute(writer, args).flush();

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("api", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("from", "ReservaLink <" + emailFrom + ">");
            body.add("to", userEmail);
            body.add("subject", motive.getSubject());
            body.add("html", writer.toString());

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    "https://api.mailgun.net/v3/" + domain + "/messages",
                    request,
                    String.class
            );

            log.info("Email sent via Mailgun to {}", userEmail);

        } catch (Exception e) {
            log.error("Mailgun error sending email to {}", userEmail, e);
            throw new RuntimeException(
                    "Error sending email notification for motive " + motive.name(), e
            );
        }
    }
}
