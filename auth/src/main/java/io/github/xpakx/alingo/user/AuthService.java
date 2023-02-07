package io.github.xpakx.alingo.user;

import io.github.xpakx.alingo.user.dto.AuthenticationRequest;
import io.github.xpakx.alingo.user.dto.AuthenticationResponse;
import io.github.xpakx.alingo.user.dto.RegistrationRequest;

public interface AuthService {
    AuthenticationResponse register(RegistrationRequest request);
    AuthenticationResponse generateAuthenticationToken(AuthenticationRequest authenticationRequest);
}
