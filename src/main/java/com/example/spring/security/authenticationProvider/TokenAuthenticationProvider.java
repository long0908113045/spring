package com.example.spring.security.authenticationProvider;

import com.example.spring.security.Utils.TokenUtil;
import com.example.spring.security.authentication.AuthenticationToken;
import com.example.spring.security.exception.CommonAuthenticationException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!StringUtils.hasText(authentication.getPrincipal().toString())) {
            throw new CommonAuthenticationException("User key must not be empty.");
        }
        String username = null;
        String jwtToken;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (authentication.getPrincipal().toString().startsWith("Bearer ")) {
            jwtToken = authentication.getPrincipal().toString().substring(7);
            try {
                username = tokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        } else {
            throw new CommonAuthenticationException("JWT Token does not begin with Bearer String");
        }

        // Once we get the token validate it.
        if (username != null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set
            // authentication
            if (tokenUtil.validateToken(jwtToken, userDetails)) {
                return new AuthenticationToken(
                        authentication.getPrincipal().toString(), userDetails.getAuthorities());
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (AuthenticationToken.class.isAssignableFrom(authentication));
    }
}
