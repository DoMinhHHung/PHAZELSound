package iuh.fit.se.phazelsound.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendRegisterOtp(String toEmail, String name, String otp) {
        try {
            log.info("Bắt đầu gửi OTP tới: {}", toEmail);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);

            String htmlContent = templateEngine.process("email/register-otp", context);

            helper.setTo(toEmail);
            helper.setSubject("[Phazel Sound] Mã xác thực tài khoản");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            log.info("Gửi mail thành công cho {}", toEmail);

        } catch (MessagingException e) {
            log.error("Lỗi sấp mặt khi gửi mail: {}", e.getMessage());
        }
    }
    @Async
    public void sendForgotPasswordOtp(String toEmail, String name, String otp) {
        try {
            log.info("Gửi OTP quên mật khẩu tới: {}", toEmail);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);

            String htmlContent = templateEngine.process("email/forgot-password", context);

            helper.setTo(toEmail);
            helper.setSubject("[Phazel Sound] Đặt lại mật khẩu");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("Lỗi gửi mail reset pass: {}", e.getMessage());
        }
    }
}
