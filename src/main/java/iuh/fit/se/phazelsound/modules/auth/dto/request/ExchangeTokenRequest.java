package iuh.fit.se.phazelsound.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ExchangeTokenRequest {
    @NotBlank(message = "Token không được để trống")
    private String token;
}
