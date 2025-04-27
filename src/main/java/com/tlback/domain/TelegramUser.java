package com.tlback.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(schema = "public", name = "telegram_user")
public class TelegramUser {

    @Id
    @Transient
    private TelegramUserPk pk;

    // A unique identifier for the user or bot. It has at most 52 significant bits, so a 64-bit integer or a double-precision float type is safe for storing this identifier.
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    // First name of the user or bot.
    @Column("first_name")
    private String firstName;

    // Optional. True, if this user is a bot. Returns in the receiver field only.
    @Column("is_bot")
    @Nullable
    private Boolean isBot;

    // Optional. Last name of the user or bot.
    @Column("last_name")
    @Nullable
    private String lastName;

    // Optional. Username of the user or bot.
    @Column("username")
    @Nullable
    private String username;

    // Optional. IETF language tag of the user's language. Returns in user field only.
    @Column("language_code")
    @Nullable
    private String languageCode;

    // Optional. True, if this user is a Telegram Premium user.
    @Column("is_premium")
    @Nullable
    private Boolean isPremium;

    // Optional. True, if this user added the bot to the attachment menu.
    @Column("added_to_attachment_menu")
    @Nullable
    private Boolean addedToAttachmentMenu;

    // Optional. True, if this user allowed the bot to message them.
    @Column("allows_write_to_pm")
    @Nullable
    private Boolean allowsWriteToPm;

    // Optional. URL of the user’s profile photo. The photo can be in .jpeg or .svg formats.
    @Column("photo_url")
    @Nullable
    private String photoUrl;
}