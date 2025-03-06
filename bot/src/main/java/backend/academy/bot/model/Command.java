package backend.academy.bot.model;

@FunctionalInterface
public interface Command {
    void execute(Long chatId, String[] params);
}
