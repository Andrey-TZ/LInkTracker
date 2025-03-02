package backend.academy.bot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Objects;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {
    @Mock
    private ScrapperClient scrapperClient;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private CommandProcessor commandProcessor;


    @ParameterizedTest
    @ValueSource(strings = {
        "https://github.com/user/repo",
        "https://stackoverflow.com/questions/12345",
        "http://example.com"
    })
    void trackLink_Valid(String url) {
        Long chatId = 10100L;
        String command = "\track " + url;

        commandProcessor.trackLink(chatId, command);

        verify(scrapperClient, times(1)).sendLink(eq(chatId), argThat(link -> link.url().equals(url)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "github.com/user/repo",
        "https://stackoverflow",
        "http://example."
    })
    void trackLink_Invalid(String url) {
        long chatId = 10100L;
        String command = "\track " + url;

        commandProcessor.trackLink(chatId, command);

        verify(eventPublisher, times(1)).publishEvent(argThat(event -> {
            if (!(event instanceof UpdateMessage)) return false;

            UpdateMessage message = (UpdateMessage) event;
            return Objects.equals(message.chatId(), chatId) &&
                message.message().equals("Введите валидную ссылку");
        }));
    }

    @Test
    void trackLink_NoLink() {
        long chatId = 10100L;
        String command = "/track ";

        commandProcessor.trackLink(chatId, command);

        verify(eventPublisher, times(1)).publishEvent(argThat(event -> {
            if (!(event instanceof UpdateMessage)) return false;

            UpdateMessage message = (UpdateMessage) event;
            return Objects.equals(message.chatId(), chatId) &&
                message.message().equals("Используйте: /track [ссылка]");
        }));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "\tracl",
        "/hhelp",
        "  "
    })
    void processCommand_DontExist(String command){
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
        long chatId = 10100L;
        Assertions.assertFalse(commandProcessor.processCommand(chatId, command));
    }
}
