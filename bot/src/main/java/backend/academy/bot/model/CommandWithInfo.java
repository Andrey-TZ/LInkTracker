package backend.academy.bot.model;

import lombok.Getter;

@Getter
public class CommandWithInfo {
    private final Command command;
    private final String description;
    private final boolean isRequireArgs;

    public CommandWithInfo(Command command, String description, boolean isRequireArgs) {
        this.command = command;
        this.description = description;
        this.isRequireArgs = isRequireArgs;
    }
}
