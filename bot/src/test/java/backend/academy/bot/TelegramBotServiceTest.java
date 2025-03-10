package backend.academy.bot;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.model.CommandWithInfo;
import backend.academy.bot.model.UserContext;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {
    long chatId;
    String botToken;

    @Mock
    CommandProcessor commandProcessor;

    TelegramBotService telegramBotService;

    @BeforeEach
    void setUp() {
        botToken = " ";
        chatId = 1L;
        when(commandProcessor.commands())
                .thenReturn(Map.of(
                        "/start",
                                new CommandWithInfo(
                                        (Long user, String[] args) -> commandProcessor.start(user),
                                        "/start - регистрация",
                                        false),
                        "/track",
                                new CommandWithInfo(
                                        commandProcessor::trackLink, "/track [ссылка] - отслеживание", true),
                        "/untrack",
                                new CommandWithInfo(
                                        commandProcessor::untrackLink, "/untrack [ссылка] - отмена отслеживания", true),
                        "/list",
                                new CommandWithInfo(
                                        (Long user, String[] args) -> commandProcessor.list(user),
                                        "/list - список ссылок",
                                        false),
                        "/help",
                                new CommandWithInfo(
                                        (Long user, String[] args) -> commandProcessor.help(user),
                                        "/help - список команд",
                                        false)));
        telegramBotService = new TelegramBotService(botToken, commandProcessor);
    }

    static Stream<Arguments> argsProvider() {
        return Stream.of(
                Arguments.of((Object) new String[] {"/track", "https://stackoverflow.com/", "study"}),
                Arguments.of((Object) new String[] {"/untrack", "https://stackoverflow.com/"}),
                Arguments.of((Object) new String[] {"/list"}),
                Arguments.of((Object) new String[] {"/start"}),
                Arguments.of((Object) new String[] {"/help"}));
    }

    @Test
    void handleCommand_Track() {
        String[] args = {"/track", "https://stackoverflow.com/", "study"};
        UserContext context = new UserContext(TelegramBotService.DialogState.AWAITING_COMMAND);
        telegramBotService.waitCommand(chatId, args, context);

        verify(commandProcessor, times(1)).trackLink(chatId, args);
    }

    @Test
    void handleCommand_Untrack() {
        String[] args = {"/untrack", "https://stackoverflow.com/"};
        UserContext context = new UserContext(TelegramBotService.DialogState.AWAITING_COMMAND);
        telegramBotService.waitCommand(chatId, args, context);

        verify(commandProcessor, times(1)).untrackLink(chatId, args);
    }

    @Test
    void handleCommand_Start() {
        String[] args = {"/start"};
        UserContext context = new UserContext(TelegramBotService.DialogState.AWAITING_COMMAND);
        telegramBotService.waitCommand(chatId, args, context);

        verify(commandProcessor, times(1)).start(chatId);
    }

    @Test
    void handleCommand_List() {
        String[] args = {"/list"};
        UserContext context = new UserContext(TelegramBotService.DialogState.AWAITING_COMMAND);
        telegramBotService.waitCommand(chatId, args, context);

        verify(commandProcessor, times(1)).list(chatId);
    }

    @Test
    void handleCommand_Help() {
        String[] args = {"/help"};
        UserContext context = new UserContext(TelegramBotService.DialogState.AWAITING_COMMAND);
        telegramBotService.waitCommand(chatId, args, context);

        verify(commandProcessor, times(1)).help(chatId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/hep", "/go", "start", "track", "/unrack"})
    void handleInvalidCommand(String command) {
        String[] args = {command};
        UserContext context = new UserContext(TelegramBotService.DialogState.AWAITING_COMMAND);

        Assertions.assertThrows(
                IllegalArgumentException.class, () -> telegramBotService.waitCommand(chatId, args, context));
    }
}
