package backend.academy.scrapper.services.link;

import backend.academy.common.Link;
import backend.academy.scrapper.entities.LinkEntity;
import backend.academy.scrapper.entities.Tag;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.LinkAlreadyExistsException;
import backend.academy.scrapper.exceptions.LinkNotFoundException;
import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import backend.academy.scrapper.exceptions.UserNotFoundException;
import backend.academy.scrapper.repos.JPALinkRepository;
import backend.academy.scrapper.repos.JPATagRepository;
import backend.academy.scrapper.repos.JPAUserRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class JPALinkService implements LinkService {
    private final JPAUserRepository userRepository;
    private final JPATagRepository tagRepository;
    private final JPALinkRepository linkRepository;

    @Autowired
    public JPALinkService(
            @Qualifier("jpaRepo") JPAUserRepository userRepository,
            JPATagRepository tagRepository,
            JPALinkRepository linkRepository) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.linkRepository = linkRepository;
    }

    @Transactional
    @Override
    public void addUser(long chatId) {
        User user = new User();
        user.chatId(chatId);
        if (userRepository.findByChatId(chatId).isEmpty()) {
            userRepository.save(user);
        } else {
            throw new UserAlreadyExistsException("Пользователь уже зарегистрирован");
        }
    }

    @Transactional
    @Override
    public void addLink(long chatId, Link link) {
        User user = userRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не существует"));

        if (linkRepository.existsByUserAndUrl(user, link.url())) {
            throw new LinkAlreadyExistsException("Ссылка уже отслеживается");
        }

        LinkEntity linkEntity = new LinkEntity();
        linkEntity.url(link.url());
        linkEntity.creationDate(link.updatedAt());
        linkEntity.user(user);

        Set<Tag> tags = new HashSet<>();
        for (String tagName : link.tags()) {
            Tag tag = tagRepository.findByTag(tagName).orElseGet(() -> {
                Tag newTag = new Tag();
                newTag.tag(tagName);
                return tagRepository.save(newTag);
            });
            tags.add(tag);
        }
        linkEntity.tags(tags);
        user.linkEntitySet().add(linkEntity);
        userRepository.save(user);
        //        linkRepository.save(linkEntity);
    }

    @Transactional
    @Override
    public void deleteLink(long chatId, Link link) {
        User user = userRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не существует"));

        LinkEntity linkToDelete = user.linkEntitySet().stream()
                .filter(l -> l.url().equals(link.url()))
                .findFirst()
                .orElseThrow(() -> new LinkNotFoundException("Ссылка не найдена"));

        Set<Tag> tags = linkToDelete.tags();

        user.linkEntitySet().remove(linkToDelete);
        userRepository.save(user);

        // Проверка, используются ли теги удаленной ссылки
        for (Tag tag : tags) {
            if (!tagRepository.existsByLinksContaining(tag)) {
                tagRepository.delete(tag);
            }
        }
    }

    @Override
    public Set<Link> getLinks(long chatId) {
        User user = userRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не существует"));
        return user.linkEntitySet().stream().map(LinkEntity::toLink).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getUsers() {
        return userRepository.findAll().stream().map(User::chatId).collect(Collectors.toSet());
    }
}
