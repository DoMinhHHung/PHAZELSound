package iuh.fit.se.phazelsound.modules.auth.service;

import iuh.fit.se.phazelsound.modules.auth.dto.request.LoginUserRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.request.RegisterUserRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.request.ResetPasswordRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.response.AuthResponse;

public interface AuthService {
    String register(RegisterUserRequest request);
    String verifyRegisterOtp(String email, String otp);
    AuthResponse login(LoginUserRequest request);

    String resendRegisterOtp(String email);
    String sendForgotPasswordOtp(String email);
    String resetPassword(ResetPasswordRequest request);
}