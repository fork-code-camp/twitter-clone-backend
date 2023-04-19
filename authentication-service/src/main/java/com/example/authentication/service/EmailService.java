package com.example.authentication.service;

import com.example.authentication.entity.ActivationCode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender sender;
    private final Configuration config;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @SneakyThrows
    public void sendActivationCode(ActivationCode activationCode) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MULTIPART_MODE_MIXED_RELATED, UTF_8.name());
        Template template = config.getTemplate("email.html");

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", activationCode.getAccount().getEmail());
        map.put("code", activationCode.getKey());
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        helper.setSubject("Activation code");
        helper.setFrom(senderEmail);
        helper.setTo(activationCode.getAccount().getEmail());
        helper.setText(html, true);

        sender.send(message);
    }
}
