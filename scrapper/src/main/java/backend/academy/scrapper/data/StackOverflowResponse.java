package backend.academy.scrapper.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowResponse {
    @Getter
    @JsonProperty("items")
    private List<StackOverflowAnswer> items;
}
