package com.tlback.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "public", name = "role")
public class RoleEntity {

    public static final RoleEntity ROLE_USER = new RoleEntity(1L, "ROLE_USER");

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;
}
