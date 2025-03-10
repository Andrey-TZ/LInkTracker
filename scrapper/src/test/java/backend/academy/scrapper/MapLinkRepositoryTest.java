package backend.academy.scrapper;

import backend.academy.common.Link;
import backend.academy.scrapper.data.MapLinkRepository;
import backend.academy.scrapper.exceptions.LinkAlreadyExistsException;
import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import backend.academy.scrapper.exceptions.UserNotFoundException;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapLinkRepositoryTest {
    MapLinkRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MapLinkRepository();
    }

    @Test
    void addUser_getLinks() {
        long chatId = 10L;

        repository.addUser(chatId);

        Assertions.assertTrue(repository.getLinks(chatId).isEmpty());
    }

    @Test
    void addUser_Double() {
        long chatId = 10L;

        repository.addUser(chatId);

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.addUser(chatId));
    }

    @Test
    void addLink_NoUser() {
        long chatId = 10L;
        Link link = new Link("https://stackoverflow.com/", new String[] {"job"});

        Assertions.assertThrows(UserNotFoundException.class, () -> repository.addLink(chatId, link));
    }

    @Test
    void addLink_Double() {
        long chatId = 10L;
        Link link1 = new Link("https://stackoverflow.com/", new String[] {"job"});
        Link link2 = new Link("https://stackoverflow.com/", new String[] {"study"});

        repository.addUser(chatId);
        repository.addLink(chatId, link1);

        Assertions.assertThrows(LinkAlreadyExistsException.class, () -> repository.addLink(chatId, link2));
    }

    @Test
    void addLink() {
        long chatId = 10L;
        Link link = new Link("https://stackoverflow.com/", new String[] {"job"});

        repository.addUser(chatId);
        repository.addLink(chatId, link);
        Set<Link> links = repository.getLinks(chatId);
        Iterator<Link> iterator = links.iterator();
        Link link_gotten = iterator.next();

        Assertions.assertEquals(link, link_gotten);
    }

    @Test
    void addLink_delete() {
        long chatId = 10L;
        Link link = new Link("https://stackoverflow.com/", new String[] {"job"});
        Link link_to_delete = new Link("https://stackoverflow.com/");

        repository.addUser(chatId);
        repository.addLink(chatId, link);
        repository.deleteLink(chatId, link_to_delete);

        Assertions.assertTrue(repository.getLinks(chatId).isEmpty());
    }
}
