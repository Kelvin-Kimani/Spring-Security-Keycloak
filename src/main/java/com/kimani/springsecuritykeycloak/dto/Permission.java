package com.kimani.springsecuritykeycloak.dto;

import org.springframework.security.core.GrantedAuthority;

public class Permission implements GrantedAuthority {

    private String permissionName;

    private Permission(){}

    public Permission(String permissionName){
        this.permissionName = permissionName;
    }

    @Override
    public String getAuthority() {
        return permissionName;
    }
}
