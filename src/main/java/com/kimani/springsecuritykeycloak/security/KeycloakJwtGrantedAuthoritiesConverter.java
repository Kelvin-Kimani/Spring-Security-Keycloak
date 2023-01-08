package com.kimani.springsecuritykeycloak.security;

import com.kimani.springsecuritykeycloak.dto.Permission;
import com.kimani.springsecuritykeycloak.dto.Role;
import com.kimani.springsecuritykeycloak.security.inter.RolesConfigProperties;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES = "roles";
    private static final String RESOURCE_ACCESS = "resource_access";
    private final String clientId;
    private final RolesConfigProperties rolesConfigProperties;

    @SuppressWarnings("unused")
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public KeycloakJwtGrantedAuthoritiesConverter(String clientId, RolesConfigProperties rolesConfigProperties) {
        this.clientId = clientId;
        this.rolesConfigProperties = rolesConfigProperties;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt theJwt) {
        JSONObject keycloakClientAuthorities = ((JSONObject) theJwt.getClaimAsMap(RESOURCE_ACCESS).get(clientId));
        if (keycloakClientAuthorities != null) {
            JSONArray parsedRoles = (JSONArray) keycloakClientAuthorities.get(ROLES);
            if (parsedRoles != null && Objects.nonNull(rolesConfigProperties.getRolePermissionMap())) {
                Collection<GrantedAuthority> clientRoles = parsedRoles.stream()
                        .map(aRole -> new Role((String) aRole))
                        .collect(Collectors.toList());

                try {
                    Collection<GrantedAuthority> clientPermissions = clientRoles.stream()
                            .filter(r -> Objects.nonNull(r.getAuthority()))
                            .flatMap(r -> rolesConfigProperties.getRolePermissionMap().get(r.getAuthority()).stream())
                            .map(Permission::new)
                            .collect(Collectors.toList());

                    clientRoles.addAll(clientPermissions);
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                }

                return clientRoles;
            }
        }

        return new ArrayList<>();
    }
}
