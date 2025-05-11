package backend.academy.scrapper.services;

import backend.academy.common.Update;
import backend.academy.scrapper.conditions.HttpTransportEnabledCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Conditional(HttpTransportEnabledCondition.class)
@Primary
@Component
public class HttpNotificationService implements NotificationService {
    private final WebClient webClient;

    public HttpNotificationService() {
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
