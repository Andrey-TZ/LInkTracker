package backend.academy.scrapper.services.link;

import backend.academy.common.Link;
import java.util.Set;

public interface LinkService {
    void addUser(long chatId);

    void addLink(long chatId, Link link);

    void deleteLink(long chatId, Link link);

    Set<Link> getLinks(long chatId);

    Set<Long> getUsers();
}
