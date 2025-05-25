package com.tlback.model;

import java.io.Serializable;

import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUserPk implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column("id")
    private Long id;
    @Column("user_id")
    private Long userId;
}
