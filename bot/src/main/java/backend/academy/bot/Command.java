package backend.academy.bot;

@FunctionalInterface
public interface Command {
    public void execute(Long chatId, String params);
}
