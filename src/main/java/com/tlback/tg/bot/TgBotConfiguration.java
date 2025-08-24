package com.tlback.tg.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TgBotConfiguration {

	@Value("${GLAM_TG_BOT_TOKEN}")
	private String botToken;

	@Value("${app.baseUrl}")
	private String appBaseUrl;

}