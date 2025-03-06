package backend.academy.scrapper.clients;

import backend.academy.common.Link;

public interface APIClient {
    boolean getUpdates(long chatId, Link link);
}
