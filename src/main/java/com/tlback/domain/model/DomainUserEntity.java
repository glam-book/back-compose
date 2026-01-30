package com.tlback.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.tlback.domain.model.contact.UserContactType;

import io.vavr.control.Option;
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

    @Column("profile_icon")
    private String profileIcon;

    private Option<TelegramUser> tgUser;

    public Object getSubuser(UserContactType source) {
        return switch(source) {
            case TG -> tgUser;
            default -> tgUser;
        };
    }

    public List<UserContactType> determineSupportedContacts() {
        var initial = new ArrayList<UserContactType>();

        if (!tgUser.isEmpty())
            initial.add(UserContactType.TG);

        return initial;
    }
}
