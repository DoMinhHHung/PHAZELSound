package iuh.fit.se.phazelsound.modules.auth.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
}