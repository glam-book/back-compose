package com.tlback.tg.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TgBotConfiguration {

	@Value("${GLAM_TG_BOT_TOKEN}")
	private String botToken;

	@Value("${app.baseUrl}")
	private String appBaseUrl;

	@Bean
	TgGlamBot bot() {
		return new TgGlamBot(botToken, AppArgs.TG_WEB_HOOK_PATH);
	}

	@Bean
	TgGlamLongPolingBot longPolingBot() {
		return new TgGlamLongPolingBot(botToken);
	}

	@Bean
	TgBotInitializer botInitializer(TgGlamLongPolingBot bot) {
		return new TgBotInitializer(bot);
	}

	public static class TgBotInitializer {
		private final TgGlamLongPolingBot bot;

		public TgBotInitializer(TgGlamLongPolingBot bot) {
			this.bot = bot;
		}

		@EventListener(ContextRefreshedEvent.class)
		public void init() throws Exception {
			var botApis = new TelegramBotsApi(DefaultBotSession.class);
			botApis.registerBot(bot);
		}
	}
}