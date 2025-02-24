package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TelegramBotService {
    private final TelegramBot bot;
    private final Map<Long, Set<String>> userLinks = new HashMap<>();
    private final Map<String, CommandWithInfo> commands;

    public TelegramBotService(@Value("${app.telegram-token}") String botToken, @Autowired CommandProcessor commandProcessor) {
        this.bot = new TelegramBot(botToken);
        commandProcessor.registerCommand("/start", (Long chatId, String args) -> commandProcessor.start(chatId),
            "/start - регистрация пользователя");
        commandProcessor.registerCommand("/track", commandProcessor::trackLink,
            "/track [ссылка] - начать отслеживание ссылки");
        commandProcessor.registerCommand("/untrack", commandProcessor::untrackLink,
            "/untrack [ссылка] - прекратить отслеживание ссылки");
        commandProcessor.registerCommand("/list", (Long chatId, String args) -> commandProcessor.list(chatId),
            "/list - список отслеживаемых ссылок");
        commandProcessor.registerCommand("/help", (Long chatId, String args) -> commandProcessor.help(chatId),
            "/help - список команд");

        commands = commandProcessor.commands();

    }

    @PostConstruct
    public void start() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    long chatID = update.message().chat().id();
                    String text = update.message().text().trim();

                    try {
                        // Вызов команды
                        commands.get(text.split(" ")[0]).getCommand().execute(chatID, text);
                    } catch (NullPointerException | ClassCastException e) {
                        sendMessage(chatID, "Неизвестная команда. Используйте /help для списка доступных команд.");
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendMessage(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    @Async
    @EventListener(UpdateMessage.class)
    public void handleUpdateMessage(UpdateMessage event) {
        bot.execute(new SendMessage(event.chatId(), event.message()));
    }


}
