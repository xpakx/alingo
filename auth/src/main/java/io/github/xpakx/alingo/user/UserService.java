package io.github.xpakx.alingo.user;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final AccountRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account userAccount = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user with username " + username));
        return new User(String.valueOf(userAccount.getId()), userAccount.getPassword(), userAccount.getRoles());
    }

    public UserDetails userAccountToUserDetails(Account userAccount) {
        return new User(String.valueOf(userAccount.getId()), userAccount.getPassword(), userAccount.getRoles());
    }
}
