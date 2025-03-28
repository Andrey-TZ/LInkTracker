package backend.academy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Link {
    private Long id;

    @JsonProperty("url")
    private String url;

    @JsonProperty("tags")
    private String[] tags;

    @JsonProperty("date")
    private LocalDateTime date;

    public Link(String url) {
        this.url = url;
        date = LocalDateTime.now();
    }

    public Link(String url, String[] tags) {
        this.url = url;
        this.tags = tags;
        date = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(url, link.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public void setDate() {
        date = LocalDateTime.now();
    }
}
