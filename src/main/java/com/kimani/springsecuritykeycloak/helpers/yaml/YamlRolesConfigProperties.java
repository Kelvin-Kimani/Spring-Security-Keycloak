package com.kimani.springsecuritykeycloak.helpers.yaml;

import com.kimani.springsecuritykeycloak.dto.RoleGrantedAuthority;
import com.kimani.springsecuritykeycloak.security.inter.RolesConfigProperties;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@NoArgsConstructor
@PropertySource(value = "classpath:conf/roles.yaml", factory = YamlPropertySourceFactory.class)
public class YamlRolesConfigProperties implements RolesConfigProperties {

    @Bean
    @ConfigurationProperties(prefix = "permission-mapping.roles")
    public List<RoleGrantedAuthority> grantedAuthorities(){
        return new ArrayList<>();
    }

     public Map<String, List<String>> getRolePermissionMap(){
        return grantedAuthorities().stream().collect(Collectors.toMap(RoleGrantedAuthority::getName, role -> {
            List<String> permissions = new ArrayList<>();
            List<String> addedRoles = new ArrayList<>();
            addedRoles.add(role.getName());
            extractPermissions(role, addedRoles, permissions);
            return permissions;
        }));
    }

    //This may generate a circular loop should an embedded role refer to its parent
    private void extractPermissions(RoleGrantedAuthority role, List<String> rolesAdded, List<String> permissions) {
        if(role.getPermissions()!=null){
            permissions.addAll(role.getPermissions());
        }

        if(role.getRoles()!=null){
            grantedAuthorities().stream()
                    .filter(r-> r.getName()!=null && role.getRoles().contains(r.getName()))
                    .forEach(embeddedRole -> {
                        if(!rolesAdded.contains(embeddedRole.getName())){
                            log.trace("Extracting embedded role: "+embeddedRole.getName());
                            rolesAdded.add(embeddedRole.getName());//This is to manage circular references
                            extractPermissions(embeddedRole,rolesAdded, permissions);
                        }
                    });
        }
    }
}
