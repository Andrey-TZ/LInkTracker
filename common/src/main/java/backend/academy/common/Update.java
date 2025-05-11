package backend.academy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Update implements Serializable {
    @JsonProperty("link")
    private String link;

    @JsonProperty("messages")
    private List<String> messages;
}
