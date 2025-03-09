package backend.academy.bot;

import backend.academy.bot.model.CommandWithInfo;
import backend.academy.bot.model.UpdateMessage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
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
    private final Map<String, CommandWithInfo> commands;
    private final Map<Long, UserContext> userContexts = new HashMap<>();

    public TelegramBotService(
            @Value("${app.telegram-token}") String botToken, @Autowired CommandProcessor commandProcessor) {
        this.bot = new TelegramBot(botToken);
        commandProcessor.registerCommand(
                "/start",
                (Long chatId, String[] args) -> commandProcessor.start(chatId),
                "/start - регистрация пользователя",
                false);
        commandProcessor.registerCommand(
                "/track", commandProcessor::trackLink, "/track [ссылка] - начать отслеживание ссылки", true);
        commandProcessor.registerCommand(
                "/untrack", commandProcessor::untrackLink, "/untrack [ссылка] - прекратить отслеживание ссылки", true);
        commandProcessor.registerCommand(
                "/list",
                (Long chatId, String[] args) -> commandProcessor.list(chatId),
                "/list - список отслеживаемых ссылок",
                false);
        commandProcessor.registerCommand(
                "/help", (Long chatId, String[] args) -> commandProcessor.help(chatId), "/help - список команд", false);
        log.atInfo().setMessage("Команды зарегистрированы").log();
        commands = commandProcessor.commands();
    }

    @PostConstruct
    public void start() {
        bot.setUpdatesListener(updates -> {
            log.atInfo().setMessage("Начато слушание сообщений").log();
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    long chatId = update.message().chat().id();
                    String[] args = update.message().text().trim().split(" ");
                    if (!userContexts.containsKey(chatId)) {
                        userContexts.put(chatId, new UserContext(DialogState.AWAITING_COMMAND));
                        log.atInfo()
                                .setMessage("Начат диалог с пользователем")
                                .addKeyValue("user", chatId)
                                .log();
                    }
                    UserContext context = userContexts.get(chatId);
                    if (context.state() == DialogState.AWAITING_COMMAND) {
                        try {
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
                            } else {
                                switch (commandName) {
                                    case "/track":
                                        if (args.length > 2) {
                                            log.atDebug()
                                                    .setMessage("Команда передана сразу с аргументами и вызвана")
                                                    .addKeyValue("user", chatId)
                                                    .addKeyValue("command", commandName)
                                                    .addKeyValue("link", args[1])
                                                    .addKeyValue("tag", args[2])
                                                    .log();
                                            commands.get(commandName).command().execute(chatId, args);
                                        } else if (args.length == 2) {
                                            log.atDebug()
                                                    .setMessage("Ожидание ввода тегов")
                                                    .addKeyValue("user", chatId)
                                                    .log();
                                            context.commandName(commandName);
                                            context.state(DialogState.AWAITING_TAGS);
                                            context.addArg(args[1]);
                                            sendMessage(chatId, "Введите теги");
                                        } else {
                                            log.atDebug()
                                                    .setMessage("Ожидание ввода ссылки для отслеживания")
                                                    .addKeyValue("user", chatId)
                                                    .log();
                                            context.commandName(commandName);
                                            context.state(DialogState.AWAITING_LINK);
                                            sendMessage(chatId, "Введите ссылку");
                                        }
                                        break;
                                    case "/unrack":
                                        if (args.length == 2) {
                                            log.atDebug()
                                                    .setMessage("Команда передана сразу с аргументами и вызвана")
                                                    .addKeyValue("user", chatId)
                                                    .addKeyValue("command", commandName)
                                                    .addKeyValue("link", args[1])
                                                    .log();
                                            commands.get(commandName).command().execute(chatId, args);
                                        } else {
                                            log.atDebug()
                                                    .setMessage("Ожидание ввода ссылки для прекращения отслеживания")
                                                    .addKeyValue("user", chatId)
                                                    .log();
                                            context.commandName(commandName);
                                            context.state(DialogState.AWAITING_LINK);
                                            context.commandName(commandName);
                                            context.state(DialogState.AWAITING_LINK);
                                            sendMessage(chatId, "Введите ссылку");
                                        }
                                        break;
                                    default:
                                        log.atWarn()
                                                .setMessage("Неизвестное состояние")
                                                .addKeyValue("user", chatId)
                                                .addKeyValue("command", commandName)
                                                .addKeyValue("state", context.state)
                                                .log();
                                        sendMessage(
                                                chatId,
                                                "Неизвестная команда. Используйте /help для списка доступных команд.");
                                        break;
                                }
                            }
                        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | ClassCastException e) {
                            log.atWarn()
                                    .setMessage("Неизвестная команда")
                                    .addKeyValue("user", chatId)
                                    .addKeyValue("args", args)
                                    .log();
                            sendMessage(chatId, "Неизвестная команда. Используйте /help для списка доступных команд.");
                        }
                    } else {
                        handleDialogStep(chatId, args, context);
                    }
                }
            }
            log.atInfo().setMessage("Прослушивание завершено").log();
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    void handleDialogStep(long chatId, String[] args, UserContext context) {
        log.atDebug()
                .setMessage("Обработка состояния")
                .addKeyValue("user", chatId)
                .addKeyValue("command", context.commandName())
                .addKeyValue("state", context.state())
                .log();
        switch (context.state()) {
            case AWAITING_LINK:
                if (args.length < 1) {
                    sendMessage(chatId, "Введите ссылку");
                }
                switch (context.commandName()) {
                    case "/track":
                        sendMessage(chatId, "Введите теги");
                        context.state = DialogState.AWAITING_TAGS;
                        context.addArg(args[0]);
                        break;
                    case "/untrack":
                        context.state = DialogState.AWAITING_COMMAND;
                        context.addArg(args[0]);
                        commands.get("/untrack").command().execute(chatId, context.getArgs());
                        break;
                    default:
                        sendMessage(chatId, "Ошибка. Начните заново.");
                        context.state = DialogState.AWAITING_COMMAND;
                        break;
                }
                break;
            case AWAITING_TAGS:
                if (args.length >= 1) {
                    context.state = DialogState.AWAITING_COMMAND;
                    context.addArg(Arrays.toString(args));
                    commands.get(context.commandName()).command().execute(chatId, context.getArgs());
                } else {
                    sendMessage(chatId, "Введите теги");
                }
                break;
            default:
                sendMessage(chatId, "Ошибка. Начните заново.");
                context.state = DialogState.AWAITING_COMMAND;
                break;
        }
        userContexts.put(chatId, context);
    }

    public void sendMessage(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    @Async
    @EventListener(UpdateMessage.class)
    public void handleUpdateMessage(UpdateMessage event) {
        bot.execute(new SendMessage(event.chatId(), event.message()));
    }

    enum DialogState {
        AWAITING_COMMAND, // Пользователь не в диалоге
        AWAITING_LINK, // Ожидание ссылки для /track и /untrack
        AWAITING_TAGS, // Ожидание тегов для /track
    }

    @Getter
    class UserContext {
        @Setter
        private DialogState state;

        private final Map<String, String> params = new HashMap<>();
        private List<String> args = new ArrayList<>();
        private String commandName;

        UserContext(DialogState state) {
            this.state = state;
        }

        void commandName(String commandName) {
            this.commandName = commandName;
            addArg(commandName);
        }

        void addParam(String key, String value) {
            params.put(key, value);
        }

        void addArg(String arg) {
            args.add(arg);
        }

        String[] getArgs() {
            return args.toArray(String[]::new);
        }

        void deleteArgs() {
            args = new ArrayList<>();
        }
    }
}
