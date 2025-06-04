package com.codepipeline.mcp.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @SuppressWarnings("unchecked")
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        // Convert realm roles and resource access roles to authorities
        return Stream.concat(
            roles.stream()
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> new SimpleGrantedAuthority(role)),
            
            // Handle client roles if needed
            ((Map<String, Map<String, Object>>) jwt.getClaim("resource_access"))
                .values()
                .stream()
                .flatMap(resource -> {
                    List<String> resourceRoles = (List<String>) resource.get("roles");
                    return resourceRoles != null ? resourceRoles.stream() : Stream.empty();
                })
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        ).collect(Collectors.toList());
    }
}
