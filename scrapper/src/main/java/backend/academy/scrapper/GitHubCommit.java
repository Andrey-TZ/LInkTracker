package backend.academy.scrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCommit {
    @JsonProperty("commit")  // Соответствует "commit" в JSON
    private Commit commit;

    public String getMessage() {
        return commit != null ? commit.message : "No message";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Commit {
        @JsonProperty("message")
        private String message;
    }
}
