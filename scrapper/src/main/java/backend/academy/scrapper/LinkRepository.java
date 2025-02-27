package backend.academy.scrapper;

import backend.academy.common.Link;
import java.util.List;
import java.util.Set;

public interface LinkRepository {
    void addUser(long chatId);

    void addLink(long chatId, Link link);

    boolean deleteLink(long chatId, Link link);

    List<Link> getLinks(long chatId);

    Set<Long> getUsers();
}
