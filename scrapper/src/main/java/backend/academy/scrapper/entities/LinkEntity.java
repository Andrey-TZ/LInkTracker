package backend.academy.scrapper.entities;

import backend.academy.common.Link;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Table(name = "link")
@Entity
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime creationDate;

    @JoinColumn(name = "userId", nullable = false, referencedColumnName = "id")
    @ManyToOne
    private User user;

    @ManyToMany
    @JoinTable(
            name = "link_tag",
            joinColumns = @JoinColumn(name = "link"),
            inverseJoinColumns = @JoinColumn(name = "tag"))
    private Set<Tag> tags = new HashSet<>();

    public Link toLink() {
        return new Link(url, tags.stream().map(Tag::tag).toArray(String[]::new), creationDate);
    }
}
