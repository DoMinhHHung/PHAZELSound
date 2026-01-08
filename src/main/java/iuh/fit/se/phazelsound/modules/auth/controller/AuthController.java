package iuh.fit.se.phazelsound.modules.auth.controller;

import iuh.fit.se.phazelsound.modules.auth.dto.request.RegisterUserRequest;
import iuh.fit.se.phazelsound.modules.auth.dto.request.ResetPasswordRequest;
import iuh.fit.se.phazelsound.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterUserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.ok(authService.verifyRegisterOtp(email, otp));
    }

    // POST /api/auth/resend-register-otp?email=...
    @PostMapping("/resend-register-otp")
    public ResponseEntity<String> resendRegisterOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendRegisterOtp(email));
    }

    // POST /api/auth/forgot-password?email=...
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendForgotPasswordOtp(email));
    }

    // POST /api/auth/reset-password (Body JSON: email, otp, newPassword)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
