package backend.academy.scrapper.data;

import backend.academy.common.Link;
import backend.academy.scrapper.exceptions.LinkAlreadyExistsException;
import backend.academy.scrapper.exceptions.LinkNotFoundException;
import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import backend.academy.scrapper.exceptions.UserNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@Primary
public class MapLinkRepository implements LinkRepository {
    private final HashMap<Long, Set<Link>> linkRepository = new HashMap<>();

    @Override
    public void addUser(long chatId) {
        if (userExists(chatId)) {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }
        linkRepository.put(chatId, new HashSet<>());
    }

    @Override
    public void addLink(long chatId, Link link) {
        if (!userExists(chatId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }
        Set<Link> links = linkRepository.get(chatId);
        if (links.contains(link)) {
            throw new LinkAlreadyExistsException("Ссылка уже отслеживается");
        }
        links.add(link);
    }

    @Override
    public void deleteLink(long chatId, Link link) {
        if (!linkRepository.containsKey(chatId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }
        Set<Link> links = linkRepository.get(chatId);
        if (!links.contains(link)) {
            throw new LinkNotFoundException("Ссылка не отслеживается ");
        }
        links.remove(link);
    }

    @Override
    public Set<Link> getLinks(long chatId) {
        if (!linkRepository.containsKey(chatId)) {
            throw new UserNotFoundException("Пользователь с chatId " + chatId + " не найден");
        }
        return linkRepository.get(chatId);
    }

    @Override
    public Set<Long> getUsers() {
        return linkRepository.keySet();
    }

    @Override
    public boolean userExists(long chatId) {
        return linkRepository.containsKey(chatId);
    }
}
