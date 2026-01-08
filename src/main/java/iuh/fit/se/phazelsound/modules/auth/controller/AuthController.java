package iuh.fit.se.phazelsound.modules.auth.controller;

import iuh.fit.se.phazelsound.modules.auth.dto.request.RegisterUserRequest;
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
}
