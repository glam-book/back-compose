package com.tlback.domain;

import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUserPk {
    @Column("id")
    private Long id;
    @Column("user_id")
    private Long userId;
}
