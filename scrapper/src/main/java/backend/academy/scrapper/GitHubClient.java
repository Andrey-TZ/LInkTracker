package backend.academy.scrapper;

import backend.academy.common.Link;
import backend.academy.common.Update;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GitHubClient implements APIClient {
    private final WebClient webClient;
    private final BotClient botClient;

    @Autowired
    public GitHubClient(@Value("${app.github-token}") String githubToken, BotClient botClient) {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.github.com/")
            .defaultHeader("Authorization", "token " + githubToken)
            .build();
        this.botClient = botClient;
    }

    public void getCommitMessages(String owner, String repo, String since, String link, long chatId) {
        webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/repos/{owner}/{repo}/commits")
                .queryParam("since", since)
                .build(owner, repo))
            .retrieve()
            .bodyToFlux(GitHubCommit.class)
            .map(GitHubCommit::getMessage)
            .collectList()
            .subscribe(response -> botClient.sendUpdate(chatId, new Update(link, response))); // что потом делать с этим списком
    }


    @Override
    public boolean getUpdates(long chatId, Link link) {
        try {
            URI uri = new URI(link.url());
            String host = uri.getHost();
            if (!host.endsWith("github.com")) {
                return false;
            }
            String[] path = uri.getPath().split("/");
            getCommitMessages(path[1], path[2], link.date().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME), link.url(), chatId);
            return true;
        } catch (URISyntaxException | IndexOutOfBoundsException e) {
            return false;
        }
    }
}
