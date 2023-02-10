package io.github.xpakx.alingo.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class RegistrationRequest {
    @NotBlank
    @Length(min=5, max=15)
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String passwordRe;

    @AssertTrue(message = "Passwords don't match!")
    private boolean isPasswordRepeated() {
        return password.equals(passwordRe);
    }
}
