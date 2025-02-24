package backend.academy.bot;

public class CommandWithInfo {
    private final Command command;
    private final String description;

    public CommandWithInfo(Command command, String description) {
        this.command = command;
        this.description = description;
    }

    public Command getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }
}
