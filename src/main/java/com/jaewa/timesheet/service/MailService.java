package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Locale;


@Log4j2
@Service
public class MailService {

    private static final String USER = "user";
    private static final String RANDOM_PASSWORD = "randomPassword";

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    private final String mailFrom;

    public MailService(
            JavaMailSender emailSender,
            TemplateEngine templateEngine,
            @Value("${timesheet.email.from}") String mailFrom
    ) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.mailFrom = mailFrom;
    }

    public void sendActivationEmail(ApplicationUser user, String randomPassword) {
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable(USER, user.getFirstName());
        context.setVariable(RANDOM_PASSWORD, randomPassword);
        sendEmail(user.getEmail(), "mail/activationEmail", context, "Jaewa Timesheet - Attivazione Account");
    }

    public void sendEmail(String sendTo, String templateName, Context context, String subject) {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        try {
            String content = templateEngine.process(templateName, context);
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            message.setTo(sendTo);
            message.setFrom(mailFrom);
            message.setSubject(subject);
            message.setText(content, true);
            emailSender.send(mimeMessage);
            log.debug("Email inviata a '{}'", sendTo);
        } catch (MailException | MessagingException e) {
            log.warn("Non è stato possibile inviare l'email a '{}'", sendTo, e);
        }
    }

    public void sendResetPasswordEmail(ApplicationUser user, String randomPassword) {
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable(USER, user.getFirstName());
        context.setVariable(RANDOM_PASSWORD, randomPassword);
        sendEmail(user.getEmail(), "mail/passwordResetEmail", context, "Jaewa Timesheet - Reset Password");
    }
}
