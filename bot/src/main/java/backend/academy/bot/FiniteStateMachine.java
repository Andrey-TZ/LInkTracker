package backend.academy.bot;

import backend.academy.bot.model.UserContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FiniteStateMachine {
    enum State{
        AWAITING_COMMAND,
        AWAITING_URL,
        AWAITING_TAG
    }

    private final Map<Long, UserContext> userStates = new ConcurrentHashMap<Long, UserContext>();
}
