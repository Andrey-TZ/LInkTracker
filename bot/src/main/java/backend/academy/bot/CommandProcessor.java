package backend.academy.bot;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import backend.academy.common.Link;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CommandProcessor {
    @Getter
    private final Map<String, CommandWithInfo> commands = new HashMap<>();
    private final ScrapperClient scrapperClient;
    private final ApplicationEventPublisher eventPublisher;

    public CommandProcessor(@Autowired ScrapperClient scrapperClient, @Autowired ApplicationEventPublisher eventPublisher) {
        this.scrapperClient = scrapperClient;
        this.eventPublisher = eventPublisher;
    }

    public boolean processCommand(long chatId, String text) {
        try {
            commands.get(text.split(" ")[0]).getCommand().execute(chatId, text);
            return true;
        } catch (NullPointerException | ClassCastException e) {
            return false;
        }
    }

    public void registerCommand(String name, Command command, String description) {
        commands.put(name, new CommandWithInfo(command, description));
    }

    public void start(long chatId) {
        scrapperClient.addUser(chatId);
        ;
    }

    public void trackLink(long chatId, String text) {
        String[] parts = text.split(" ", 2);
        if (parts.length < 2) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Используйте: /track [ссылка]"));
            return;
        }
        String link = parts[1];
        try {
            URI uri = URI.create(link);
            if (uri.isAbsolute() && uri.getScheme() != null) {
                scrapperClient.sendLink(chatId, new Link(link));
            } else {
                eventPublisher.publishEvent(new UpdateMessage(chatId, "Введите валидную ссылку"));
            }
        } catch (IllegalArgumentException e) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Введите валидную ссылку"));
        }
    }

    public void untrackLink(long chatId, String text) {
        String[] parts = text.split(" ", 2);
        if (parts.length < 2) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Используйте: /untrack [ссылка]"));
            return;
        }
        scrapperClient.deleteLink(chatId, new Link(parts[1]));
    }

    public void list(long chatId) {
//        Set<String> links = userLinks.getOrDefault(chatId, new HashSet<>());
//        if (links.isEmpty()) {
//            eventPublisher.publishEvent(new UpdateMessage(chatId, "Вы пока не отслеживаете ни одной ссылки."));
//        } else {
//            StringBuilder response = new StringBuilder("*Ваши отслеживаемые ссылки:*\n");
//            links.forEach(link -> response.append("- ").append(link).append("\n"));
//        }
        scrapperClient.getLinks(chatId);
    }

    public void help(long chatId) {
        eventPublisher.publishEvent(new UpdateMessage(chatId, "Доступные команды:\n" +
            commands.values().stream()
                .map(CommandWithInfo::getDescription)
                .collect(Collectors.joining("\n"))));
    }
}
