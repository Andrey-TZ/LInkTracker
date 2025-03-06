package backend.academy.bot;

import backend.academy.bot.model.Command;
import backend.academy.bot.model.CommandWithInfo;
import backend.academy.bot.model.UpdateMessage;
import backend.academy.common.Link;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandProcessor {
    private static final String DOMAIN_REGEX = "(?i)^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Getter
    private final Map<String, CommandWithInfo> commands = new HashMap<>();

    private final ScrapperClient scrapperClient;
    private final ApplicationEventPublisher eventPublisher;

    public CommandProcessor(
            @Autowired ScrapperClient scrapperClient, @Autowired ApplicationEventPublisher eventPublisher) {
        this.scrapperClient = scrapperClient;
        this.eventPublisher = eventPublisher;
    }

    public void registerCommand(String name, Command command, String description, boolean isRequireArgs) {
        commands.put(name, new CommandWithInfo(command, description, isRequireArgs));
    }

    public void start(long chatId) {
        scrapperClient.addUser(chatId);
    }

    public void trackLink(long chatId, String[] args) {
        if (args.length < 2) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Используйте: /track [ссылка]"));
            return;
        }
        String link = args[1];
        String[] tags = args[2].split(" ");
        try {
            URI uri = URI.create(link);
            String host = uri.getHost();
            if (uri.isAbsolute() && uri.getScheme() != null && host != null && host.matches(DOMAIN_REGEX)) {
                scrapperClient.sendLink(chatId, new Link(link, tags));
            } else {
                log.info("Введена невалидная ссылка");
                eventPublisher.publishEvent(new UpdateMessage(chatId, "Введите валидную ссылку"));
            }
        } catch (IllegalArgumentException e) {
            log.error("Неверный формант аргумента команды /track: {}", e.getMessage());
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Введите валидную ссылку"));
        }
    }

    public void untrackLink(long chatId, String[] args) {
        if (args.length < 2) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Используйте: /untrack [ссылка]"));
            return;
        }
        scrapperClient.deleteLink(chatId, new Link(args[1]));
    }

    public void list(long chatId) {
        scrapperClient.getLinks(chatId);
    }

    public void help(long chatId) {
        eventPublisher.publishEvent(new UpdateMessage(
                chatId,
                "Доступные команды:\n"
                        + commands.values().stream()
                                .map(CommandWithInfo::description)
                                .collect(Collectors.joining("\n"))));
    }
}
