package backend.academy.scrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowResponse {
    @Getter
    @JsonProperty("items")
    private List<StackOverflowAnswer> items;
}
