package iuh.fit.se.phazelsound.modules.auth.service.impl;

import iuh.fit.se.phazelsound.common.service.EmailService;
import iuh.fit.se.phazelsound.modules.auth.dto.request.LoginUserRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.request.RegisterUserRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.request.ResetPasswordRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.response.AuthResponse;
import iuh.fit.se.phazelsound.modules.auth.entity.AuthProvider;
import iuh.fit.se.phazelsound.modules.auth.entity.UserRole;
import iuh.fit.se.phazelsound.modules.auth.service.AuthService;
import iuh.fit.se.phazelsound.modules.auth.service.JwtService;
import iuh.fit.se.phazelsound.modules.user.entity.User;
import iuh.fit.se.phazelsound.modules.user.entity.UserStatus;
import iuh.fit.se.phazelsound.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.otp.expiration-minutes}")
    private long otpExpirationMinutes;

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
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(otpExpirationMinutes));

        log.info("Saved OTP to Redis: Key={}, OTP={}", redisKey, otp);
        emailService.sendRegisterOtp(request.getEmail(), request.getName(), otp);

        return "Registration successful. Please check your email to verify your account!";
    }

    @Override
    public String verifyRegisterOtp(String email, String otp) {
        String redisKey = "OTP_REGISTER:" + email;
        Object storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new RuntimeException("The OTP code has expired or is incorrect.");
        }

        if (!storedOtp.toString().equals(otp)) {
            throw new RuntimeException("OTP incorrect.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Not found user."));

        if (user.getStatus() == UserStatus.ACTIVE) {
            redisTemplate.delete(redisKey);
            return "This account has been activated.";
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        redisTemplate.delete(redisKey);

        return "Verification successful. Account has been activated.";
    }

    @Override
    public AuthResponse login(LoginUserRequest request) {
        User user = userRepository.findByEmailOrPhone(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Not found user."));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public String resendRegisterOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered."));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("This account has been activated.");
        }

        String otp = generateOtp();
        String redisKey = "OTP_REGISTER:" + email;
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(otpExpirationMinutes));

        emailService.sendRegisterOtp(email, user.getFullName(), otp);

        return "The OTP has been sent. Please check your email or spam folder.";
    }

    @Override
    public String sendForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found."));

        String otp = generateOtp();

        String redisKey = "OTP_FORGOT:" + email;
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(otpExpirationMinutes));

        emailService.sendForgotPasswordOtp(email, user.getFullName(), otp);

        return "The password reset OTP has been sent. Please check your email or spam folder.";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        String redisKey = "OTP_FORGOT:" + request.getEmail();
        Object storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new RuntimeException("The OTP has expired or does not exist.");
        }
        if (!storedOtp.toString().equals(request.getOtp())) {
            throw new RuntimeException("OTP incorrect.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Not found user."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(redisKey);

        return "Password reset successful";
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
