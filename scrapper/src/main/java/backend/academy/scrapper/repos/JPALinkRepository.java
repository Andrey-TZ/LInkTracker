package backend.academy.scrapper.repos;

import backend.academy.scrapper.entities.LinkEntity;
import backend.academy.scrapper.entities.User;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Conditional(JPARepositoryEnabledCondition.class)
public interface JPALinkRepository extends JpaRepository<LinkEntity, Long> {
    boolean existsByUserAndUrl(User user, String url);
}
