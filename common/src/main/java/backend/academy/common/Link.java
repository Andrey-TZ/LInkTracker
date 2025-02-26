package backend.academy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@EqualsAndHashCode
@Setter
@Getter
@NoArgsConstructor
public class Link {
    @JsonProperty("url")
    private String url;
    @JsonProperty("tags")
    private List<String> tags;

    public Link(String url) {
        this.url = url;
    }
}
