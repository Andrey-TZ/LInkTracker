package backend.academy.scrapper;

import backend.academy.scrapper.data.GitHubIssue;
import backend.academy.scrapper.data.StackOverflowQuestion;
import backend.academy.scrapper.data.StackOverflowResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

class ParserTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testStackoverflowParsing() throws IOException {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("StackOverflowDataTest.json")) {
            StackOverflowResponse response = objectMapper.readValue(inputStream, StackOverflowResponse.class);
            StackOverflowQuestion question = response.items().getFirst();

            int comments = 2;
            int answers = 2;
            String title = "What is the difference between putIfAbsent and computeIfAbsent in Java 8 Map ?";
            Long answerCreationDate = 1515576389L;

            Assertions.assertEquals(comments, question.comments().size());
            Assertions.assertEquals(answers, question.answers().size());
            Assertions.assertEquals(
                    answerCreationDate, question.answers().getFirst().creationDate());
            Assertions.assertEquals(title, question.title());
        }
    }

    @Test
    void testGitHubParsing() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("GitHubDataTest.json")) {
            List<GitHubIssue> response = objectMapper.readValue(inputStream, new TypeReference<List<GitHubIssue>>() {});

            int responseSize = 2;
            String login1 = "Shershah03";
            String date1 = "2025-03-23T18:49:46Z";

            Assertions.assertEquals(responseSize, response.size());
            Assertions.assertEquals(login1, response.getFirst().login());
            Assertions.assertEquals(date1, response.getFirst().created());
        }
    }
}
