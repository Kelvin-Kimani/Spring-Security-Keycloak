package com.kimani.springsecuritykeycloak.security.inter;

import java.util.List;
import java.util.Map;

public interface RolesConfigProperties {
    Map<String, List<String>> getRolePermissionMap();
}
