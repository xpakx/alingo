package io.github.xpakx.alingo.user.graphql;

import io.github.xpakx.alingo.user.AuthService;
import io.github.xpakx.alingo.user.dto.AuthenticationRequest;
import io.github.xpakx.alingo.user.dto.AuthenticationResponse;
import io.github.xpakx.alingo.user.dto.RegistrationRequest;
import io.github.xpakx.alingo.user.graphql.validation.RepeatedPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphAuthController {
    private final AuthService service;

    @MutationMapping
    public AuthenticationResponse login(@NotBlank(message = "Username cannot be empty") @Argument String username,
                                        @NotBlank(message = "Password cannot be empty") @Argument String password) {
        return service.generateAuthenticationToken(toAuthRequest(username, password));
    }

    @MutationMapping
    @RepeatedPassword
    public AuthenticationResponse register(@NotBlank(message = "Username cannot be empty") @Length(min=5, max=15, message = "Username length must be between 5 and 15") @Argument String username,
                                           @NotBlank(message = "Password cannot be empty") @Argument String password,
                                           @Argument String passwordRe) {
        return service.register(toRegistrationRequest(username, password, passwordRe));
    }

    private AuthenticationRequest toAuthRequest(String username, String password) {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private RegistrationRequest toRegistrationRequest(String username, String password, String passwordRe) {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setPasswordRe(passwordRe);
        return request;
    }
}
