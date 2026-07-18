package backend.academy.bot;

import backend.academy.bot.model.CommandWithInfo;
import backend.academy.bot.model.UserMessage;
import backend.academy.bot.model.UserContext;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TelegramBotService {
    private static final String DOMAIN_REGEX = "(?i)^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final TelegramBot bot;
    private final Map<String, CommandWithInfo> commands;
    private final Map<Long, UserContext> userContexts = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(8); // пул на 8 потоков


    public TelegramBotService(String botToken, CommandProcessor commandProcessor) {
        this.bot = new TelegramBot(botToken);
        commands = commandProcessor.commands();
        registerTelegramCommands();
    }

    void registerTelegramCommands() {
        List<BotCommand> botCommandsList = new ArrayList<>();
        for (Map.Entry<String, CommandWithInfo> command : commands.entrySet()) {
            botCommandsList.add(
                new BotCommand(command.getKey(), command.getValue().description()));
        }
        BotCommand[] botCommandsArray = botCommandsList.toArray(new BotCommand[0]);
        BaseResponse response = bot.execute(new SetMyCommands(botCommandsArray));

        if (response.isOk()) {
            log.atInfo().setMessage("Команды зарегистрированы в боте").log();
        } else {
            log.atError()
                .setMessage("Не удалось зарегистрировать команды в боте")
                .log();
        }
    }

    @PostConstruct
    public void start() {
        log.atInfo().setMessage("Начало проверки обновлений").log();
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() == null || update.message().text() == null) {
                    continue;
                }
                executor.submit(() -> handleUpdate(update));

            }
            log.atInfo().setMessage("Прослушивание завершено").log();
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    void handleUpdate(Update update) {
        long chatId = update.message().chat().id();
        String[] args = update.message().text().trim().split(" ");
        UserContext context =
            userContexts.computeIfAbsent(chatId, k -> new UserContext(DialogState.AWAITING_COMMAND));
        try {
            switch (context.state()) {
                case AWAITING_COMMAND -> waitCommand(chatId, args, context);
                case AWAITING_LINK -> waitLink(chatId, args, context);
                case AWAITING_TAGS -> waitTags(chatId, args, context);
                case READY_TO_EXECUTE -> executeCommand(chatId, context);
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | ClassCastException e) {
            log.atWarn()
                .setMessage("Неизвестная команда")
                .addKeyValue("user", chatId)
                .addKeyValue("args", args)
                .log();
            sendMessage(chatId, "Неизвестная команда. Используйте /help для списка доступных команд.");
        }
    }

    void waitCommand(long chatId, String[] args, UserContext context) {
        String commandName = args[0];
        CommandWithInfo command = commands.get(commandName);
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        if (!command.isRequireArgs()) {
            context.state(DialogState.READY_TO_EXECUTE);
            return;
        }
        context.commandName(commandName);
        sendMessage(chatId, "Введите URL");
        context.state(DialogState.AWAITING_LINK);
    }

    boolean checkUri(String link) {
        URI uri = URI.create(link);
        String host = uri.getHost();
        return uri.isAbsolute() && uri.getScheme() != null && host != null && host.matches(DOMAIN_REGEX);
    }

    void waitLink(long chatId, String[] args, UserContext context) {
        if (checkUri(args[0])) {
            context.addArg(args[0]);
        } else {
            sendMessage(chatId, "Введен некорректный URL, введите заново");
            return;
        }
        switch (context.commandName()) {
            case "/track":
                sendMessage(chatId, "Введите теги через пробел");
                context.state(DialogState.AWAITING_TAGS);
                break;
            case "/untrack":
                context.state(DialogState.READY_TO_EXECUTE);
                break;
            default:
                sendMessage(chatId, "Ошибка. Начните заново.");
                context.state(DialogState.AWAITING_COMMAND);
                context.clearArgs();
                break;
        }
    }

    void waitTags(long chatId, String[] args, UserContext context) {
        if (args.length >= 1) {
            context.addArg(Arrays.toString(args));
            context.state(DialogState.READY_TO_EXECUTE);
        } else {
            sendMessage(chatId, "Введите теги через пробел");
        }
    }

    void executeCommand(long chatId, UserContext context) {
        commands.get(context.commandName()).command().execute(chatId, context.getArgs());
        context.state(DialogState.AWAITING_COMMAND);
        context.clearArgs();
    }

    public void sendMessage(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    @Async
    @EventListener(UserMessage.class)
    public void handleUpdateMessage(UserMessage event) {
        bot.execute(new SendMessage(event.chatId(), event.message()));
    }

    public enum DialogState {
        AWAITING_COMMAND, // Пользователь не в диалоге
        AWAITING_LINK, // Ожидание ссылки для /track и /untrack
        AWAITING_TAGS, // Ожидание тегов для /track
        READY_TO_EXECUTE // Готова для выполнения
    }
}
