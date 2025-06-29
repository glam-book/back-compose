package com.tlback.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tlback.tg.bot.TgBotConfiguration;

@Configuration
@Import(TgBotConfiguration.class)
public class TelegramBotConfig {
    
}
