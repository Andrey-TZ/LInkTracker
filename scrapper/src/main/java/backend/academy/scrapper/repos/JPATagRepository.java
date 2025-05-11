package backend.academy.scrapper.repos;

import backend.academy.scrapper.conditions.JPARepositoryEnabledCondition;
import backend.academy.scrapper.entities.Tag;
import java.util.Optional;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Conditional(JPARepositoryEnabledCondition.class)
public interface JPATagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTag(String tag);

    @Query(
            """
        SELECT CASE WHEN COUNT(l) > 0
        THEN true ELSE false END
        FROM LinkEntity l WHERE :tag MEMBER OF l.tags
        """)
    Boolean existsByLinksContaining(@Param("tag") Tag tag);
}
