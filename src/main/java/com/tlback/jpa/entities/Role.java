package com.tlback.jpa.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(schema = "public", name = "role")
public class Role {

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;
}
