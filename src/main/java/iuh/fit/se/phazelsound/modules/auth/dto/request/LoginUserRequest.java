package iuh.fit.se.phazelsound.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserRequest {
    @NotBlank(message = "Email or phone is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
