package backend.academy.bot;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.bot.model.UserMessage;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {
    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommandProcessor commandProcessor;

    @ParameterizedTest
    @ValueSource(
            strings = {"https://github.com/user/repo", "https://stackoverflow.com/questions/12345", "http://example.com"
            })
    void trackLink_Valid(String url) {
        // Arrange
        Long chatId = 10100L;
        String[] command = {"\track ", url, "valid"};

        // Act
        commandProcessor.trackLink(chatId, command);

        // Assert
        verify(scrapperClient, times(1))
                .sendLink(eq(chatId), argThat(link -> link.url().equals(url)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"github.com/user/repo", "https://stackoverflow", "http://example."})
    void trackLink_Invalid(String url) {
        // Arrange
        long chatId = 10100L;
        String[] command = {"\track ", url, "invalid"};

        // Act
        commandProcessor.trackLink(chatId, command);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(argThat(event -> {
            if (!(event instanceof UserMessage)) return false;

            UserMessage message = (UserMessage) event;
            return Objects.equals(message.chatId(), chatId) && message.message().equals("Введена невалидная ссылка");
        }));
    }

    @Test
    void trackLink_NoLink() {
        // Arrange
        long chatId = 10100L;
        String[] command = {"/track "};

        // Act
        commandProcessor.trackLink(chatId, command);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(argThat(event -> {
            if (!(event instanceof UserMessage)) return false;

            UserMessage message = (UserMessage) event;
            return Objects.equals(message.chatId(), chatId) && message.message().equals("Используйте: /track [ссылка]");
        }));
    }
}
