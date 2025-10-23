package owpk.ogovpn.domain.tg.handlers;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import owpk.ogovpn.tg.balancer.TelegramClientGroupping;
import owpk.ogovpn.tg.handlers.TgAbsMessageHandler;

@Component
public class TgMessageHandlerImpl extends TgAbsMessageHandler {

    @Override
    protected Map<String, CommandHandler> registerCommandHandlers() {
        return Map.of("/start", startCommandHandler());
    }

    protected CommandHandler startCommandHandler() {
        var txt = """
                👋 Рады видеть вас в нашем боте!

                🔐 Безопасное подключение
                Это приложение помогает безопасно подключаться к вашему удалённому оборудованию или локальной инфраструктуре.
                Используется защищённый канал связи, позволяющий работать с внутренними сервисами, как будто вы находитесь рядом.

                🗂️ Доступ к нужным ресурсам
                Панели управления, внутренние сайты, файлы, учётные системы и другие привычные сервисы — прямо с вашего устройства.

                👨‍💻 Подходит для
                Удалённой работы, учёбы, администрирования и технической поддержки.

                ⚙️ Простая настройка
                Соединение устанавливается автоматически, не требует специальных знаний или конфигураций.

                🎁 Попробуйте бесплатно месяц!

                👉 Для настройки нажмите кнопку ‘Settings’
                """;

        return (msg, client) -> { 
            var tgMessage = SendMessage.builder()
                .chatId(msg.getChatId())
                .text(txt)
                .build();

            client.executeGeneric(tgMessage);
        };
    }

    @Override
    protected void handleSimpleText(String txt, String chatId, TelegramClientGroupping tgClient) {
    }

}
