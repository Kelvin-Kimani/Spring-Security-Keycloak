package com.kimani.springsecuritykeycloak.security;

import com.kimani.springsecuritykeycloak.exceptions.UnknownIssuerException;
import com.kimani.springsecuritykeycloak.security.inter.IssuerResolver;
import com.kimani.springsecuritykeycloak.security.inter.RolesConfigProperties;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MultiRealmAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {
    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();
    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();
    private final String clientId;
    private final IssuerResolver issuerSolver;
    private final RolesConfigProperties rolesConfigProperties;


    public MultiRealmAuthenticationManagerResolver(String clientId, IssuerResolver issuerSolver
            , RolesConfigProperties rolesConfigProperties) {
        this.clientId = clientId;
        this.issuerSolver = issuerSolver;
        this.rolesConfigProperties = rolesConfigProperties;
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String realm = toRealm(request);
        if (realm == null || realm.isBlank()) return null;
        return this.authenticationManagers.computeIfAbsent(realm, this::fromIssuer);
    }

    public String toRealm(HttpServletRequest request) {
        try {
            String token = this.resolver.resolve(request);
            if (token == null) return null;

            JWT parser = JWTParser.parse(token);
            String algorithm = parser.getHeader().getAlgorithm().getName();
            if (!algorithm.equals("RS256")) {
                return null;
            }

            //RS256 - utilizing public key - currently provided by Keycloak
            JWTClaimsSet claims = parser.getJWTClaimsSet();
            String realmId = claims.getStringClaim("realmId");
            if (realmId == null) {
                //We could not resolve from claims
                //resolve from issuer? http://idp:9999/auth/realms/master
                String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
                if (issuer != null) {
                    String[] tokens = issuer.split("/");
                    realmId = tokens[tokens.length - 1];
                }
            }
            return realmId;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public AuthenticationManager fromIssuer(String realmId) {
        return Optional.ofNullable(issuerSolver.getIssuerByRealmId(realmId))
                .map(issuerUri -> NimbusJwtDecoder.withJwkSetUri(issuerUri + "/protocol/openid-connect/certs")
                        .build())
                .map(decoder -> {
                    var provider = new JwtAuthenticationProvider(decoder);
                    var converter = new JwtAuthenticationConverter();
                    if (clientId != null)
                        converter.setJwtGrantedAuthoritiesConverter
                                (new KeycloakJwtGrantedAuthoritiesConverter(clientId, rolesConfigProperties));
                    provider.setJwtAuthenticationConverter(converter);
                    return provider;
                })
                .orElseThrow(UnknownIssuerException::new)::authenticate;
    }
}
