package backend.academy.scrapper.services.link;

import backend.academy.common.Link;
import backend.academy.scrapper.exceptions.LinkAlreadyExistsException;
import backend.academy.scrapper.exceptions.LinkNotFoundException;
import backend.academy.scrapper.exceptions.LinkServiceException;
import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import backend.academy.scrapper.exceptions.UserNotFoundException;
import backend.academy.scrapper.repos.LinkRepository;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

public class JDBCLinkService implements LinkService {
    private final LinkRepository repository;

    @Autowired
    public JDBCLinkService(@Qualifier("jdbcRepo") LinkRepository repo) {
        repository = repo;
    }

    @Override
    public void addUser(long chatId) {
        if (repository.userExists(chatId)) {
            throw new UserAlreadyExistsException("Пользователь уже зарегистрирован");
        }
        repository.addChat(chatId);
    }

    @Transactional
    @Override
    public void addLink(long chatId, Link link) {
        Optional<Long> userId = repository.findUserIdByChatId(chatId);
        if (userId.isEmpty()) {
            throw new UserNotFoundException("Пользователь не существует");
        }

        if (repository.linkExists(chatId, link.url())) {
            throw new LinkAlreadyExistsException(String.format("Ссылка %s уже отслеживается", link.url()));
        }

        Optional<Long> linkId = repository.addLink(userId.orElseThrow(), link.url(), link.updatedAt());
        if (linkId.isEmpty()) {
            throw new LinkServiceException("Не удалось добавить ссылку");
        }

        for (String tag : link.tags()) {
            Optional<Long> tagId = repository.findTagId(tag);
            if (tagId.isEmpty()) {
                tagId = repository.addTag(tag);
                if (tagId.isEmpty()) {
                    throw new LinkServiceException("Не удалось добавить теги для ссылки. Ссылка НЕ была добавлена");
                }
            }

            repository.addLinkTag(linkId.orElseThrow(), tagId.orElseThrow());
        }
    }

    @Transactional
    @Override
    public void deleteLink(long chatId, Link link) {
        Optional<Long> userId = repository.findUserIdByChatId(chatId);
        if (userId.isEmpty()) {
            throw new UserNotFoundException("Пользователя не существует");
        }

        if (!repository.linkExists(chatId, link.url())) {
            throw new LinkNotFoundException(String.format("Ссылка %s не отслеживается", link.url()));
        }

        Optional<Long> linkId = repository.findLinkIdByUserIdAndUrl(userId.orElseThrow(), link.url());

        if (linkId.isEmpty()) {
            throw new LinkServiceException("Не получилось удалить ссылку из отслеживаемых");
        }

        repository.deleteLinkTagByLink(linkId.orElseThrow());
        repository.deleteLink(linkId.orElseThrow());
    }

    @Override
    public Set<Link> getLinks(long chatId) {
        if (!repository.userExists(chatId)) {
            throw new UserNotFoundException("Пользователь не зарегистрирован");
        }

        Optional<Long> userId = repository.findUserIdByChatId(chatId);
        if (userId.isEmpty()) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        return repository.findLinksByUserId(userId.orElseThrow());
    }

    @Override
    public Set<Long> getUsers() {
        return repository.findAllUsers();
    }

    @Override
    public Set<Long> getUsers(int bathSize, int offset) {
        return repository.findAllUsersByBatches(bathSize, offset);
    }
}
