package com.tlback.core.web.dto.user;

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
}
