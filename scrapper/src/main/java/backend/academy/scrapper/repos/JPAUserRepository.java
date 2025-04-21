package backend.academy.scrapper.repos;

import backend.academy.scrapper.entities.User;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Conditional(JPARepositoryEnabledCondition.class)
public interface JPAUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatId(Long chatId);

    @Query("SELECT u.chat_id FROM User u ORDER BY u.id")
    Set<Long> findAllUsersBatches(Pageable pageable);
}
