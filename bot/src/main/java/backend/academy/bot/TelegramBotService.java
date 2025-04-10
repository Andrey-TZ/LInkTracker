package backend.academy.bot;

import backend.academy.bot.model.CommandWithInfo;
import backend.academy.bot.model.UpdateMessage;
import backend.academy.bot.model.UserContext;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TelegramBotService {
    private final TelegramBot bot;
    private final Map<String, CommandWithInfo> commands;
    private final Map<Long, UserContext> userContexts = new ConcurrentHashMap<>();

    public TelegramBotService(String botToken, CommandProcessor commandProcessor) {
        this.bot = new TelegramBot(botToken);
        commands = commandProcessor.commands();
        registerCommands();
    }

    private void registerCommands() {
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
                long chatId = update.message().chat().id();
                String[] args = update.message().text().trim().split(" ");
                UserContext context =
                        userContexts.computeIfAbsent(chatId, k -> new UserContext(DialogState.AWAITING_COMMAND));
                try {
                    switch (context.state()) {
                        case AWAITING_COMMAND -> waitCommand(chatId, args, context);
                        case AWAITING_LINK -> waitLink(chatId, args, context);
                        case AWAITING_TAGS -> waitTags(chatId, args, context);
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
            log.atInfo().setMessage("Прослушивание завершено").log();
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    void executeTrack(long chatId, String[] args, UserContext context) {
        String name = "/track";

        if (args.length > 2) {
            log.atDebug()
                    .setMessage("Команда передана сразу с аргументами и вызвана")
                    .addKeyValue("user", chatId)
                    .addKeyValue("command", name)
                    .addKeyValue("link", args[1])
                    .addKeyValue("tag", args[2])
                    .log();
            commands.get(name).command().execute(chatId, args);
            context.clearArgs();
        } else if (args.length == 2) {
            log.atDebug()
                    .setMessage("Ожидание ввода тегов")
                    .addKeyValue("user", chatId)
                    .log();
            context.commandName(name);
            context.state(DialogState.AWAITING_TAGS);
            context.addArg(args[1]);
            sendMessage(chatId, "Введите теги");
        } else {
            log.atDebug()
                    .setMessage("Ожидание ввода ссылки для отслеживания")
                    .addKeyValue("user", chatId)
                    .log();
            context.commandName(name);
            context.state(DialogState.AWAITING_LINK);
            sendMessage(chatId, "Введите ссылку");
        }
    }

    void executeUntrack(long chatId, String[] args, UserContext context) {
        String name = "/untrack";
        if (args.length == 2) {
            log.atDebug()
                    .setMessage("Команда передана сразу с аргументами и вызвана")
                    .addKeyValue("user", chatId)
                    .addKeyValue("command", name)
                    .addKeyValue("link", args[1])
                    .log();
            commands.get(name).command().execute(chatId, args);
            context.clearArgs();
        } else {
            log.atDebug()
                    .setMessage("Ожидание ввода ссылки для прекращения отслеживания")
                    .addKeyValue("user", chatId)
                    .log();
            context.commandName(name);
            context.state(DialogState.AWAITING_LINK);
            context.commandName(name);
            context.state(DialogState.AWAITING_LINK);
            sendMessage(chatId, "Введите ссылку");
        }
    }

    void waitCommand(long chatId, String[] args, UserContext context) {
        String commandName = args[0];
        CommandWithInfo command = commands.get(commandName);
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        if (!command.isRequireArgs()) {
            log.atDebug()
                    .setMessage("Команда без аргументов вызвана")
                    .addKeyValue("command", commandName)
                    .log();
            command.command().execute(chatId, args);
            return;
        }
        switch (commandName) {
            case "/track" -> executeTrack(chatId, args, context);

            case "/untrack" -> executeUntrack(chatId, args, context);
            default -> {
                log.atWarn()
                        .setMessage("Неизвестное состояние")
                        .addKeyValue("user", chatId)
                        .addKeyValue("command", commandName)
                        .addKeyValue("state", context.state())
                        .log();
                sendMessage(chatId, "Неизвестная команда. Используйте /help для списка доступных команд.");
            }
        }
    }

    void waitLink(long chatId, String[] args, UserContext context) {
        if (args.length < 1) {
            sendMessage(chatId, "Введите ссылку");
        }
        switch (context.commandName()) {
            case "/track":
                sendMessage(chatId, "Введите теги");
                context.state(DialogState.AWAITING_TAGS);
                context.addArg(args[0]);
                break;
            case "/untrack":
                context.state(DialogState.AWAITING_COMMAND);
                context.addArg(args[0]);
                commands.get("/untrack").command().execute(chatId, context.getArgs());
                break;
            default:
                sendMessage(chatId, "Ошибка. Начните заново.");
                context.state(DialogState.AWAITING_COMMAND);
                break;
        }
    }

    void waitTags(long chatId, String[] args, UserContext context) {
        if (args.length >= 1) {
            context.state(DialogState.AWAITING_COMMAND);
            context.addArg(Arrays.toString(args));
            commands.get(context.commandName()).command().execute(chatId, context.getArgs());
        } else {
            sendMessage(chatId, "Введите теги");
        }
    }

    public void sendMessage(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    @Async
    @EventListener(UpdateMessage.class)
    public void handleUpdateMessage(UpdateMessage event) {
        bot.execute(new SendMessage(event.chatId(), event.message()));
    }

    public enum DialogState {
        AWAITING_COMMAND, // Пользователь не в диалоге
        AWAITING_LINK, // Ожидание ссылки для /track и /untrack
        AWAITING_TAGS, // Ожидание тегов для /track
    }
}
