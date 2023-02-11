package io.github.xpakx.alingo.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtils jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        authenticateUser(request);
        filterChain.doFilter(request, response);
    }

    private void authenticateUser(HttpServletRequest request) {
        if(this.isAuthMissing(request)) {
            logger.warn("Authorization header is missing in request");
            return;
        }

        final String token = this.getAuthHeader(request).substring(7);

        if(jwt.isInvalid(token)) {
            logger.warn("Authorization token is invalid");
            return;
        }

        Claims claims = jwt.getAllClaimsFromToken(token);

        if(claims.getSubject() != null && !isUserAlreadyAuthenticated()) {
            UserDetails userDetails = createUserDetails(claims);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, "", userDetails.getAuthorities());

            authToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }


    private boolean isUserAlreadyAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !auth.isAuthenticated() || !(auth instanceof AnonymousAuthenticationToken);
    }

    private UserDetails createUserDetails(Claims claims) {
        return new User(claims.getSubject(), "", new ArrayList<>());
    }

    private boolean isAuthMissing(HttpServletRequest request) {
        final String requestTokenHeader = this.getAuthHeader(request);
        return requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ");
    }

    private String getAuthHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}