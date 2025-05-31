package backend.academy.scrapper.services.link;

import backend.academy.common.Link;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public interface LinkService {
    void addUser(long chatId);

    @CacheEvict(value = "userLinks", key = "#chatId")
    void addLink(long chatId, Link link);

    @CacheEvict(value = "userLinks", key = "#chatId")
    void deleteLink(long chatId, Link link);

    @Cacheable(value = "userLinks", key = "#chatId")
    Set<Link> getLinks(long chatId);

    Set<Long> getUsers();

    Set<Long> getUsers(int bathSize, int offset);
}
