package backend.academy.scrapper;

import backend.academy.common.Link;

public interface APIClient {
    boolean getUpdates(long chatId, Link link);
}
