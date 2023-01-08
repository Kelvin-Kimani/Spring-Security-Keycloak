package com.kimani.springsecuritykeycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleGrantedAuthority {
    private String name;
    private String description;
    private List<String> permissions;
    private List<String> roles;
}
