package backend.academy.scrapper;

import backend.academy.common.Link;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@Primary
public class MapLinkRepository implements LinkRepository {
    private final HashMap<Long, List<Link>> linkRepository = new HashMap<>();

    @Override
    public void addUser(long chatId) {
        linkRepository.putIfAbsent(chatId, new ArrayList<>());
    }

    @Override
    public void addLink(long chatId, Link link) {
        linkRepository.putIfAbsent(chatId, new ArrayList<>());
        List<Link> links = linkRepository.get(chatId);
        links.add(link);
    }

    @Override
    public boolean deleteLink(long chatId, Link link) {
        linkRepository.putIfAbsent(chatId, new ArrayList<>());
        List<Link> links = linkRepository.get(chatId);
        boolean result = links.remove(link);
        log.info("delete link {}", result);
        return result;
    }

    @Override
    public List<Link> getLinks(long chatId) {
        return linkRepository.getOrDefault(chatId, new ArrayList<>());
    }

    @Override
    public Set<Long> getUsers() {
        return linkRepository.keySet();
    }
}
