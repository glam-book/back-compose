package com.tlback.config.security.tg;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.tlback.model.TelegramUser;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelegramAuthService {
    public static final String X_TELEGRAM_HEADER = "X-tg-data";

    private final String botToken;

    private byte[] secretHashByInitData;

    private final ObjectMapper objectMapper;

    public TelegramAuthService(String botToken) {
        this.botToken = botToken;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @PostConstruct
    public void init() {
        try {
            this.secretHashByInitData = getSecretHashByInitData();
        } catch (Exception ex) {
            log.error("[TELEGRAM_AUTH_FILTER_ERROR] init error", ex);
        }
    }

    public Optional<TelegramAuthenticationToken> auth(ServerWebExchange exchange) {
        var req = exchange.getRequest();
        var telegramHeader = req.getHeaders().getFirst(X_TELEGRAM_HEADER);

        log.info("telegram header: {}", telegramHeader);

        if (StringUtils.isBlank(telegramHeader))
            return Optional.empty();

        try {
            var params = parseQueryString(telegramHeader);
            var userBody = params.get("user");
            var hash = params.get("hash");

            if (StringUtils.isNotBlank(userBody) && StringUtils.isNotBlank(hash)
                    && validateTelegramAuth(params, hash)) {
                var telegramUser = objectMapper.readValue(userBody, TelegramUser.class);
                var auth = new TelegramAuthenticationToken(hash, telegramUser);
                return Optional.of(auth);
            }
        } catch (Exception e) {
            log.error("[TELEGRAM_AUTH_FILTER_ERROR] telegram user ", e);
        }
        return Optional.empty();
    }

    private boolean validateTelegramAuth(Map<String, String> paramMap, String receivedHash) throws Exception {
        // Пункт 1. Убираем hash и сортируем оставшиеся параметры
        var dataString = paramMap.entrySet().stream().filter(e -> !"hash".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey()).map(e -> e.getKey() + "=" + e.getValue()) // Берем первый элемент из
                                                                                              // массива параметров
                .collect(Collectors.joining("\n"));

        // Пункт 2. Создаем HMAC SHA-256 хеш
        var sha256HMAC = Mac.getInstance("HmacSHA256");
        var secretKeySpec = new SecretKeySpec(this.secretHashByInitData, "HmacSHA256");
        sha256HMAC.init(secretKeySpec);

        byte[] hash2 = sha256HMAC.doFinal(dataString.getBytes());

        // Пункт 3. Преобразуем байты хеша в строку в hex формате
        var calculatedHash = bytesToHex(hash2);

        // Пункт 4. Сравниваем полученный хеш с тем, что был в запросе
        return calculatedHash.equals(receivedHash);
    }

    private byte[] getSecretHashByInitData() throws InvalidKeyException, NoSuchAlgorithmException {
        var sha256HMAC = Mac.getInstance("HmacSHA256");
        var secretKeySpec = new SecretKeySpec("WebAppData".getBytes(), "HmacSHA256");
        sha256HMAC.init(secretKeySpec);

        return sha256HMAC.doFinal(botToken.getBytes());
    }

    private Map<String, String> parseQueryString(String queryString) {
        var result = new HashMap<String, String>();
        var pairs = queryString.split("&");

        for (String pair : pairs) {
            var keyValue = pair.split("=", 2);
            var key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            var value = URLDecoder.decode(keyValue.length > 1 ? keyValue[1] : "", StandardCharsets.UTF_8);
            result.put(key, value);
        }

        return result;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
