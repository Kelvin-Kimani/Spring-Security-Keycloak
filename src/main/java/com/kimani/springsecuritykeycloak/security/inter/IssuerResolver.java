package com.kimani.springsecuritykeycloak.security.inter;

public interface IssuerResolver {
    String getIssuerByRealmId(String realmId);
}
