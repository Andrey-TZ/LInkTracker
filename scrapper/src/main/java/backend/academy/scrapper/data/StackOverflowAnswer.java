package backend.academy.scrapper.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowAnswer {
    @JsonProperty("owner")
    private User user;

    @JsonProperty("creation_date")
    private Long creationDate;

    @JsonProperty("body_markdown")
    private String message;

    public String createNotificationMessage() {
        return String.format(
                "Дата создания: %s%nАвтор: %s%nСодержание: %s",
                creationDate, user, message.length() <= 200 ? message : message.substring(0, 199));
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class User {
        @JsonProperty("display_name")
        private String name;
    }
}
