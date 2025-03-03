package backend.academy.scrapper;

import backend.academy.common.Link;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SendService {
    private final WebClient webClient;

    public SendService() {
        this.webClient = WebClient.builder().baseUrl("http://localhost:8080").build();
    }

    public void sendUpdate(long userId, Link link) {
        webClient
                .post()
                .uri("/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(link)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
