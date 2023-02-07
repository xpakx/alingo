package io.github.xpakx.alingo.user;

import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.user.dto.AuthenticationRequest;
import io.github.xpakx.alingo.user.dto.AuthenticationResponse;
import io.github.xpakx.alingo.user.dto.RegistrationRequest;
import io.github.xpakx.alingo.user.error.AuthenticationException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AccountRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticationResponse register(RegistrationRequest request) {
        Account userToAdd = createNewUser(request);
        authenticate(request.getUsername(), request.getPassword());
        final String token = jwtUtils.generateToken(userService.userAccountToUserDetails(userToAdd));
        return AuthenticationResponse.builder()
                .token(token)
                .username(userToAdd.getUsername())
                .build();
    }

    private Account createNewUser(RegistrationRequest request) {
        Set<UserRole> roles = new HashSet<>();
        Account userToAdd = new Account();
        userToAdd.setPassword(passwordEncoder.encode(request.getPassword()));
        userToAdd.setUsername(request.getUsername());
        userToAdd.setRoles(roles);
        return userRepository.save(userToAdd);
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new AuthenticationException("User " +username+" disabled!");
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid password!");
        }
    }

    @Override
    public AuthenticationResponse generateAuthenticationToken(AuthenticationRequest authenticationRequest) {
        return null;
    }
}
