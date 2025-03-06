package backend.academy.scrapper.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowAnswer {
    @Getter
    @JsonProperty("body_markdown")
    private String message;
}
