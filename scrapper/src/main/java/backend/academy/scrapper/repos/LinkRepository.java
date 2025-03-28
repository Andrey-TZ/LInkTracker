package backend.academy.scrapper.repos;

import backend.academy.common.Link;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface LinkRepository {
    Optional<Long> addChat(long chatId);

    Optional<Long> addLink(long userId, String url, LocalDateTime creationDate);

    Optional<Long> addLink(long userId, String url, String filter, LocalDateTime creationDate);

    Optional<Long> addTag(String tag);

    void addLinkTag(long linkId, long tagId);

    void deleteLink(long linkId);

    void deleteTag(long tagId);

    void deleteLinkTag(long linkId, long tagId);

    Optional<Long> findChatById(long userId);

    Optional<Long> findUserIdByChatId(long chatId);

    Set<Link> findLinksByUserIdAndTag(long userId, long tagId);

    Set<Long> findAllUsers();

    Set<Link> findLinksByUserId(long userId);
}
