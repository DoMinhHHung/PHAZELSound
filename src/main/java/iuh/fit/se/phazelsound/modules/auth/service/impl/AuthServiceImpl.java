package iuh.fit.se.phazelsound.modules.auth.service.impl;

import iuh.fit.se.phazelsound.common.service.EmailService;
import iuh.fit.se.phazelsound.modules.auth.dto.request.RegisterUserRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.request.ResetPasswordRequest;
import iuh.fit.se.phazelsound.modules.auth.entity.AuthProvider;
import iuh.fit.se.phazelsound.modules.auth.entity.UserRole;
import iuh.fit.se.phazelsound.modules.auth.service.AuthService;
import iuh.fit.se.phazelsound.modules.user.entity.User;
import iuh.fit.se.phazelsound.modules.user.entity.UserStatus;
import iuh.fit.se.phazelsound.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    @Override
    public String register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exist!");
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhone(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exist!");
        }

        User newUser = User.builder()
                .fullName(request.getName())
                .email(request.getEmail())
                .phone(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .status(UserStatus.UNVERIFIED)
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(newUser);

        String otp = generateOtp();
        String redisKey = "OTP_REGISTER:" + request.getEmail();
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(5));

        log.info("Saved OTP to Redis: Key={}, OTP={}", redisKey, otp);
        emailService.sendRegisterOtp(request.getEmail(), request.getName(), otp);

        return "Registration successful. Please check your email to verify your account!";
    }

    @Override
    public String verifyRegisterOtp(String email, String otp) {
        String redisKey = "OTP_REGISTER:" + email;

        Object storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new RuntimeException("OTP has expired or is incorrect.");
        }

        if (!storedOtp.toString().equals(otp)) {
            throw new RuntimeException("OTP has expired or is incorrect..");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Not found user."));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        redisTemplate.delete(redisKey);

        return "Verification successful! Account has been activated.";
    }

    @Override
    public String resendRegisterOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email này chưa đăng ký mà?"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản này đã kích hoạt rồi, đăng nhập đi ba.");
        }

        String otp = generateOtp();
        String redisKey = "OTP_REGISTER:" + email;
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(5));

        emailService.sendRegisterOtp(email, user.getFullName(), otp);

        return "Đã gửi lại OTP đăng ký. Check mail đi.";
    }

    @Override
    public String sendForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));

        String otp = generateOtp();

        String redisKey = "OTP_FORGOT:" + email;
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(5));

        emailService.sendForgotPasswordOtp(email, user.getFullName(), otp);

        return "Đã gửi OTP đặt lại mật khẩu. Check mail.";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        String redisKey = "OTP_FORGOT:" + request.getEmail();
        Object storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new RuntimeException("OTP hết hạn hoặc không tồn tại.");
        }
        if (!storedOtp.toString().equals(request.getOtp())) {
            throw new RuntimeException("OTP sai rồi.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User không tồn tại."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(redisKey);

        return "Đặt lại mật khẩu thành công! Giờ đăng nhập bằng pass mới đi.";
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
