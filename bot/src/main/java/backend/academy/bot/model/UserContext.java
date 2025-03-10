package backend.academy.bot.model;

import backend.academy.bot.TelegramBotService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
public class UserContext {
    @Setter
    private TelegramBotService.DialogState state;

    private final Map<String, String> params = new HashMap<>();
    private List<String> args = new ArrayList<>();
    private String commandName;

    public UserContext(TelegramBotService.DialogState state) {
        this.state = state;
    }

    public void commandName(String commandName) {
        this.commandName = commandName;
        addArg(commandName);
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void addArg(String arg) {
        args.add(arg);
    }

    public String[] getArgs() {
        return args.toArray(String[]::new);
    }

    public void deleteArgs() {
        args = new ArrayList<>();
    }
}
