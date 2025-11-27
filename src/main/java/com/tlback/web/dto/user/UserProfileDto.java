package com.tlback.web.dto.user;

import java.util.List;

import com.tlback.core.model.contact.ContactProvider;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String name;
    private String lastName;
    private String middleName;
    private String login;

    private String profileIcon;
    private List<ContactProvider> contacts;
}
