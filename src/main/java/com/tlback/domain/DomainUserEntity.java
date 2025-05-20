package com.tlback.domain;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(schema = "public", name = "domain_user")
public class DomainUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("last_name")
    private String lastName;

    @Column("middle_name")
    private String middleName;

    @Column("login")
    private String login;

    private Optional<TelegramUser> tgUser;
}
