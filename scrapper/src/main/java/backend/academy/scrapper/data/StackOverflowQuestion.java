package backend.academy.scrapper.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowQuestion {
    @JsonProperty("title")
    private String title;

    @JsonProperty("answers")
    private List<StackOverflowAnswer> answers;

    @JsonProperty("comments")
    private List<StackOverflowAnswer> comments;

    public String createNotificationMessage() {
        StringBuilder builder = new StringBuilder(String.format("Обновления по вопросу: %s %n", title));

        if (!answers.isEmpty()) {
            builder.append("Новые ответы:");
            for (StackOverflowAnswer answer : answers) {
                builder.append("\n").append(answer.createNotificationMessage());
            }
        }

        if (!comments.isEmpty()) {
            builder.append("Новые комментарии:");
            for (StackOverflowAnswer comment : comments) {
                builder.append("\n").append(comment.createNotificationMessage());
            }
        }

        return builder.toString();
    }
}
