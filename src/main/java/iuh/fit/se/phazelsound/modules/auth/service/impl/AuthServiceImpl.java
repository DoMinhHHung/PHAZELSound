package iuh.fit.se.phazelsound.modules.auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import iuh.fit.se.phazelsound.common.service.EmailService;
import iuh.fit.se.phazelsound.modules.auth.dto.request.ExchangeTokenRequest;
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
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
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

    @Value("${application.security.oauth2.google.client-id}")
    private String googleClientId;

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
                .message("Login successfully.")
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

    @Override
    public AuthResponse loginWithGoogle(ExchangeTokenRequest request) {
        log.info("Google Client ID: {}", googleClientId);
        log.info("Token nhận được từ Postman: {}", request.getToken());

        if (request.getToken() == null) {
            throw new RuntimeException("Token gửi lên bị NULL!");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getToken());
            if (idToken == null) {
                throw new RuntimeException("Token Google đểu hoặc đã hết hạn.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String providerId = payload.getSubject();

            User user = processSocialUser(email, name, pictureUrl, AuthProvider.GOOGLE, providerId);

            return generateAuthResponse(user);

        } catch (Exception e) {
            log.error("Lỗi Google Login: {}", e.getMessage());
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }
    }


    @Override
    public AuthResponse loginWithFacebook(ExchangeTokenRequest request) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + request.getToken();
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> data = restTemplate.getForObject(url, Map.class);

            if (data == null || data.containsKey("error")) {
                throw new RuntimeException("Token Facebook không hợp lệ.");
            }

            String email = (String) data.get("email");
            String name = (String) data.get("name");
            String providerId = (String) data.get("id");

            String pictureUrl = null;
            if (data.containsKey("picture")) {
                Map<String, Object> pictureObj = (Map<String, Object>) data.get("picture");
                Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
                pictureUrl = (String) dataObj.get("url");
            }

            if (email == null) {
                email = providerId + "@facebook.com";
            }

            // Xử lý User
            User user = processSocialUser(email, name, pictureUrl, AuthProvider.FACEBOOK, providerId);

            return generateAuthResponse(user);

        } catch (Exception e) {
            log.error("Lỗi Facebook Login: {}", e.getMessage());
            throw new RuntimeException("Lỗi xác thực Facebook.");
        }
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private User processSocialUser(String email, String name, String avatarUrl, AuthProvider provider, String providerId) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(email)
                    .fullName(name)
                    .avatarUrl(avatarUrl)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .provider(provider)
                    .providerId(providerId)
                    .password(null)
                    .build();
        } else {
            user.setFullName(name);
            user.setAvatarUrl(avatarUrl);
            if (user.getProvider() == AuthProvider.LOCAL) {
                user.setProvider(provider);
                user.setProviderId(providerId);
            }
        }
        return userRepository.save(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("Login successfully with " + user.getProvider())
                .build();
    }
}
