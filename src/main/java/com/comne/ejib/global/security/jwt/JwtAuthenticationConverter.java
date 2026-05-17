package com.comne.ejib.global.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String SCOPE_CLAIM = "scope";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
        if (!JwtTokenType.ACCESS.value().equals(tokenType)) {
            throw new JwtException("Only access token can be used for authentication.");
        }
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt), jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        String scope = jwt.getClaimAsString(SCOPE_CLAIM);
        if (scope == null || scope.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(scope.split(" "))
                .filter(value -> !value.isBlank())
                .map(value -> new SimpleGrantedAuthority("ROLE_" + value))
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
