package backend.academy.scrapper.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubIssue {
    @JsonProperty("title")
    private String title;

    @JsonProperty("user")
    private User user;

    @JsonProperty("created_at")
    private String created;

    @JsonProperty("body")
    private String body;

    @JsonProperty("pull_request")
    private PullRequest pull;

    public boolean isPR() {
        return pull != null;
    }

    public String login() {
        return user.login;
    }

    public String createNotificationMessage() {
        return String.format("%s%nДата создания: %s%nАвтор: %s%nСодержание: %s", title, created, user, body);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PullRequest {
        @JsonProperty("url")
        private String url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class User {
        @JsonProperty("login")
        String login;
    }
}
