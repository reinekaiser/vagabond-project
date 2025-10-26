package com.ie207.vagabond.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendVerificationOtpEmail(String userEmail, String otp, String subject, String text) throws MessagingException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setSubject(subject);
            helper.setText(text, true);
            helper.setTo(userEmail);
            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            throw new MailSendException("Lỗi khi gửi email! Kiểm tra lại SMTP config.");
        } catch (MailException e) {
            throw new IllegalArgumentException("Không thể gửi email! " + e.getMessage());
        }
    }
}
