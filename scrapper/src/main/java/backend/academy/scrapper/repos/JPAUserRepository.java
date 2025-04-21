package backend.academy.scrapper.repos;

import backend.academy.scrapper.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatId(Long chatId);
}
