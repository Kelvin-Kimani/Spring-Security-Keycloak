package com.kimani.springsecuritykeycloak.security;

import com.kimani.springsecuritykeycloak.security.inter.RolesConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class DelegatingAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private final MultiRealmAuthenticationManagerResolver multiRealmAuthenticationManagerResolver;
    private final String secretKey;
    private final String keycloakUri;


    public DelegatingAuthenticationManagerResolver(
            @Value("${uris.root.iam}") String iamAuthUri,
            @Value("${conf.oauth.secret_key}") String secretKey,
            RolesConfigProperties rolesConfigProperties,
            @Value("${conf.oauth.client_id}") String serviceClientId) {
        this.keycloakUri = iamAuthUri;
        this.multiRealmAuthenticationManagerResolver =
                new MultiRealmAuthenticationManagerResolver(
                        serviceClientId,
                        realmId -> keycloakUri + "/realms/" + realmId,
                        rolesConfigProperties);
        this.secretKey = secretKey;
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest httpServletRequest) {
        AuthenticationManager manager = multiRealmAuthenticationManagerResolver.resolve(httpServletRequest);
        if (manager != null) {
            return manager;
        }

        //Sha 256 Authentication
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder());
        provider.setJwtAuthenticationConverter(this::convert);
        return provider::authenticate;
    }

    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HMACSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    public JwtAuthenticationToken convert(final Jwt jwt) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(token -> token.getClaimAsStringList("roles")
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        return (JwtAuthenticationToken) converter.convert(jwt);
    }
}
