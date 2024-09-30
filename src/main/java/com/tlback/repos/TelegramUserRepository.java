package com.tlback.repos;

import com.tlback.domain.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
}
