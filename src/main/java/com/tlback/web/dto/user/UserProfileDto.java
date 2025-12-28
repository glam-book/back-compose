package com.tlback.web.dto.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tlback.domain.model.contact.ContactProvider;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDto {
    private Long id;
    private String name;
    private String lastName;
    private String middleName;
    private String login;

    private String profileIcon;
    private List<ContactProvider> contacts;
}
