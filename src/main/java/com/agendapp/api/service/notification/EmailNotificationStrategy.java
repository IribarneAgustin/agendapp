package com.agendapp.api.service.notification;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.util.Map;


@Service
public class EmailNotificationStrategy implements NotificationStrategy {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MustacheFactory mustacheFactory;

    @Value("${api.email.value}")
    private String emailFrom;

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

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom);
            helper.setTo(userEmail);
            helper.setSubject(motive.getSubject());
            helper.setText(writer.toString(), true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending email notification for motive " + motive.name(), e);
        }
    }
}
