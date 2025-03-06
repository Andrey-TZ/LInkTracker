package backend.academy.scrapper.clients;

import backend.academy.common.Link;
import backend.academy.common.Update;
import backend.academy.scrapper.data.StackOverflowAnswer;
import backend.academy.scrapper.data.StackOverflowResponse;
import backend.academy.scrapper.exceptions.APIException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StackOerFlowClient implements APIClient {
    private static final String ANSWERS_FILTER = "!3vIo5Lk6ck_Z*JpBz";
    private final WebClient webClient;
    private final BotClient botClient;
    private final String key;

    @Autowired
    public StackOerFlowClient(
            @Value("${app.stackoverflow.access-token}") String accessToken,
            @Value("${app.stackoverflow.key}") String key,
            BotClient botClient) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.stackexchange.com/2.3/questions/")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();
        this.botClient = botClient;
        this.key = key;
    }

    public void getAnswers(String question, String date, String link, long chatId) {
        webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{question}/answers")
                        .queryParam("fromdate", date)
                        .queryParam("site", "stackoverflow")
                        .queryParam("filter", ANSWERS_FILTER)
                        .queryParam("key", key)
                        .build(question))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new APIException("Ошибка в работе с API" + response.statusCode())))
                .bodyToMono(StackOverflowResponse.class)
                .subscribe(
                        response -> {
                            List<String> answers = response.items().stream()
                                    .map(StackOverflowAnswer::message)
                                    .collect(Collectors.toList());
                            botClient.sendUpdate(chatId, new Update(link, answers));
                        },
                        error -> log.error("Error in StackOverflow client:{}", error.getMessage()));
    }

    @Override
    public boolean getUpdates(long chatId, Link link) {
        try {
            URI uri = new URI(link.url());
            String host = uri.getHost();
            if (!host.endsWith("stackoverflow.com")) {
                return false;
            }
            String[] path = uri.getPath().split("/");
            getAnswers(path[2], String.valueOf(link.date().toEpochSecond(ZoneOffset.UTC)), link.url(), chatId);
            return true;
        } catch (URISyntaxException | IndexOutOfBoundsException e) {
            return false;
        }
    }
}
