package com.tlback.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.tlback.core.model.contact.Supports;

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

    @Column("icon")
    private String profileIcon;

    private Optional<TelegramUser> tgUser;

    public Object getSubuser(Supports source) {
        return switch(source) {
            case TG -> tgUser;
            default -> tgUser;
        };
    }

    public List<Supports> determineSupportedContacts() {
        var initial = new ArrayList<Supports>();

        if (tgUser.isPresent())
            initial.add(Supports.TG);

        return initial;
    }
}
