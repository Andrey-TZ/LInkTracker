package backend.academy.bot;

@FunctionalInterface
public interface Command {
    void execute(Long chatId, String params);
}
