package iuh.fit.se.phazelsound.modules.auth.service;

import iuh.fit.se.phazelsound.modules.auth.dto.request.RegisterUserRequest;

public interface AuthService {
    String register(RegisterUserRequest request);
    String verifyRegisterOtp(String email, String otp);
}