package backend.academy.scrapper;

import backend.academy.common.Update;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BotClient {
    private final WebClient webClient;

    public BotClient() {
        this.webClient = WebClient.builder().baseUrl("http://localhost:8080").build();
    }

    public void sendUpdate(long chatId, Update update) {
        webClient
                .put()
                .uri("/updates/{chatId}", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
