package io.github.xpakx.alingo.user;

import io.github.xpakx.alingo.user.dto.AuthenticationRequest;
import io.github.xpakx.alingo.user.dto.AuthenticationResponse;
import io.github.xpakx.alingo.user.dto.RegistrationRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthenticationResponse register(RegistrationRequest request) {
        return null;
    }

    @Override
    public AuthenticationResponse generateAuthenticationToken(AuthenticationRequest authenticationRequest) {
        return null;
    }
}
