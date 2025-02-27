package backend.academy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Update {
    @JsonProperty("link")
    private String link;

    @JsonProperty("messages")
    private List<String> messages;
}
