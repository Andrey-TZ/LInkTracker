package backend.academy.scrapper.repos;

import backend.academy.common.Link;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository("jdbcRepo")
public class JDBCLinkRepository implements LinkRepository {
    private final JdbcClient jdbcClient;

    @Autowired
    public JDBCLinkRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Long> addChat(long chatId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql("INSERT INTO chat (chatId) VALUES (:chatId)")
                .param("chatId", chatId)
                .update(keyHolder, "id");
        return Optional.ofNullable(keyHolder.getKeyAs(Long.class));
    }

    @Override
    public Optional<Long> addLink(long userId, String url, LocalDateTime creationDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql("INSERT INTO link (url, creation_date, userId) VALUES (:url, :creation_date, :userId)")
                .param("url", url)
                .param("creation_date", creationDate)
                .param("userId", userId)
                .update(keyHolder, "id");
        return Optional.ofNullable(keyHolder.getKeyAs(Long.class));
    }

    @Override
    public Optional<Long> addLink(long userId, String url, String filter, LocalDateTime creationDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql("INSERT INTO link (url, creation_date, userId) VALUES (:url, :filter, :creation_date, :userId)")
                .param("url", url)
                .param("filter", filter)
                .param("creation_date", creationDate)
                .param("userId", userId)
                .update(keyHolder, "id");
        return Optional.ofNullable(keyHolder.getKeyAs(Long.class));
    }

    @Override
    public Optional<Long> addTag(String tag) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("INSERT INTO tag (tag) VALUES (:tag)").param("tag", tag).update(keyHolder, "id");
        return Optional.ofNullable(keyHolder.getKeyAs(Long.class));
    }

    @Override
    public void addLinkTag(long linkId, long tagId) {
        jdbcClient
                .sql("INSERT INTO link_tag (link, tag) VALUES (:linkId, :tagId)")
                .param("linkId", linkId)
                .param("tagId", tagId)
                .update();
    }

    @Override
    public void deleteLink(long linkId) {
        jdbcClient.sql("DELETE FROM link WHERE id = ? ").param(linkId).update();
    }

    @Override
    public void deleteTag(long tagId) {
        jdbcClient.sql("DELETE FROM tag WHERE id = ?").param(tagId).update();
    }

    @Override
    public void deleteLinkTagByLink(long linkId) {
        jdbcClient
                .sql("DELETE FROM link_tag WHERE link = :linkId")
                .param("linkId", linkId)
                .update();
    }

    @Override
    public void deleteLinkTag(long linkId, long tagId) {
        jdbcClient
                .sql("DELETE FROM link_tag WHERE link = :linkId AND tag = :tagId")
                .param("linkId", linkId)
                .param("tagId", tagId)
                .update();
    }

    @Override
    public Optional<Long> findChatById(long userId) {
        return jdbcClient
                .sql("SELECT chatId FROM chat WHERE id = ?")
                .param(userId)
                .query(Long.class)
                .optional();
    }

    @Override
    public Optional<Long> findTagId(String tag) {
        return jdbcClient
                .sql("SELECT id FROM tag WHERE tag = ?")
                .param(tag)
                .query(Long.class)
                .optional();
    }

    @Override
    public Optional<Long> findUserIdByChatId(long chatId) {
        return jdbcClient
                .sql("SELECT id FROM chat WHERE chatId = ?")
                .param(chatId)
                .query(Long.class)
                .optional();
    }

    @Override
    public Set<Link> findLinksByUserIdAndTag(long userId, long tagId) {
        return jdbcClient
                .sql(
                        "SELECT l.url, l.creation_date FROM link l JOIN LINK_TAG lt ON l.id = lt.link WHERE l.userId = :userId AND lt.tag = :tagId ")
                .param("userId", userId)
                .param("tagId", tagId)
                .query(new LinkRowMapper())
                .set();
    }

    @Override
    public Set<Long> findAllUsers() {
        return jdbcClient.sql("SELECT chatId FROM chat").query(Long.class).set();
    }

    @Override
    public Set<Link> findLinksByUserId(long userId) {
        return jdbcClient
                .sql("SELECT url, creation_date FROM link WHERE userId = ?")
                .param(userId)
                .query(new LinkRowMapper())
                .set();
    }

    @Override
    public Optional<Long> findLinkIdByUserIdAndUrl(long userId, String url) {
        return jdbcClient
                .sql("SELECT id FROM link WHERE userId = :userId AND url = :url")
                .param("userId", userId)
                .param("url", url)
                .query(Long.class)
                .optional();
    }

    @Override
    public boolean userExists(Long chatId) {
        return jdbcClient
                .sql("SELECT EXISTS(SELECT 1 FROM chat WHERE chatId = :chatId)")
                .param("chatId", chatId)
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean linkExists(Long chatId, String url) {
        return jdbcClient
                .sql(
                        "SELECT EXISTS(SELECT 1 FROM link JOIN chat ON link.userId = chat.id WHERE chat.chatId = :chatId AND link.url = :url)")
                .param("chatId", chatId)
                .param("url", url)
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean tagExists(String tag) {
        return jdbcClient
                .sql("SELECT EXISTS(SELECT 1 FROM tag WHERE tag = :tag)")
                .param("tag", tag)
                .query(Boolean.class)
                .single();
    }
}
