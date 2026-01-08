package iuh.fit.se.phazelsound.modules.auth.service;

import iuh.fit.se.phazelsound.modules.auth.dto.request.RegisterUserRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.request.ResetPasswordRequest;

public interface AuthService {
    String register(RegisterUserRequest request);
    String verifyRegisterOtp(String email, String otp);

    String resendRegisterOtp(String email);
    String sendForgotPasswordOtp(String email);
    String resetPassword(ResetPasswordRequest request);
}