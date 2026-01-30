package com.tlback.domain.model.contact;

public enum UserContactType {

    /**
     * Telegram user
     */
    TG, 

    /**
     * anonymous user
     */
    ANON,

    /**
     * phone number
     */
    PHONE,

    /**
     * email
     */
    EMAIL,

    /**
     * Произвольный тип контакта - например, для анонимных пользователей
     */
    RAW
}
