package com.jaewa.timesheet.service;

import com.jaewa.timesheet.exception.MailSendingException;
import com.jaewa.timesheet.model.ApplicationUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;


@Log4j2
@Service
public class MailService {

    private static final String USER = "user";
    private static final String RANDOM_PASSWORD = "randomPassword";
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    private final ApplicationUserService applicationUserService;
    private final String mailFrom;

    public MailService(
            JavaMailSender emailSender,
            TemplateEngine templateEngine,
            ApplicationUserService applicationUserService, @Value("${timesheet.email.from}") String mailFrom
    ) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.applicationUserService = applicationUserService;
        this.mailFrom = mailFrom;
    }

    public void sendActivationEmail(ApplicationUser user, String randomPassword) throws MailSendingException {
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable(USER, user.getFirstName());
        context.setVariable(RANDOM_PASSWORD, randomPassword);
        sendEmail(user.getEmail(), "mail/activationEmail", context, "Jaewa Timesheet - Attivazione Account");
    }

    public void sendWorkdaysExportByEmail(String[] array, Long userId, Integer month, Integer year, File file) throws MailSendingException {
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        ApplicationUser user = applicationUserService.findById(userId);
        context.setVariable("senderName", user.getFirstName());
        context.setVariable("senderSurname", user.getLastName());
        context.setVariable("month", month);
        context.setVariable("year", year);
        String subject = String.format("Jaewa Timesheet - Foglio ore %s %s - %s/%s", user.getFirstName(), user.getLastName(), month, year);
        sendEmailToGroup(array, "mail/workdaysExportEmail", context, subject, file);
    }

    public void sendEmail(String sendTo, String templateName, Context context, String subject) throws MailSendingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        try {
            String content = templateEngine.process(templateName, context);
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            message.setTo(sendTo);
            message.setFrom(mailFrom);
            message.setSubject(subject);
            message.setText(content, true);
            emailSender.send(mimeMessage);
            log.debug("Email has been sent to '{}'", sendTo);
        } catch (Exception e) {
            log.warn("It was impossible to send email to'{}'", sendTo, e);
            throw new MailSendException("It was impossible to send email");
        }
    }


    public void sendResetPasswordEmail(ApplicationUser user, String randomPassword) throws MailSendingException {
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable(USER, user.getFirstName());
        context.setVariable(RANDOM_PASSWORD, randomPassword);
        sendEmail(user.getEmail(), "mail/passwordResetEmail", context, "Jaewa Timesheet - Password Reset");
    }

    public void sendEmailToGroup(String[] sendTo, String templateName, Context context, String subject, File file) throws MailSendingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        String attachmentFileName = subject.replace("Jaewa - ", "").replace(" ", "_") + ".xlsx";
        try {
            String content = templateEngine.process(templateName, context);
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            message.setTo(sendTo);
            message.setFrom(mailFrom);
            message.setSubject(subject);
            message.setText(content, true);
            message.addAttachment(attachmentFileName, file);
            emailSender.send(mimeMessage);
            log.debug("Email inviate a '{}'", Arrays.toString(sendTo));
        } catch (Exception e) {
            log.warn("It was impossible to send email to'{}'", sendTo, e);
            throw new MailSendException("It was impossible to send email");
        }
    }


}
