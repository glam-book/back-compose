package com.tlback.web.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String lastName;
    private String middleName;
    private String login;
    private Set<RoleDto> roles = new HashSet<>();
}
