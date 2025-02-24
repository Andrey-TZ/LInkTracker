package backend.academy.bot;

import backend.academy.common.Link;
import backend.academy.bot.UpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ScrapperClient {
    private final WebClient webClient;
    private final ApplicationEventPublisher eventPublisher;


    public ScrapperClient(@Autowired ApplicationEventPublisher eventPublisher) {
        this.webClient = WebClient.builder()
            .baseUrl("http://localhost:8081")
            .build();
        this.eventPublisher = eventPublisher;
    }

    public void getLinks(long chatId) {
        webClient.get()
            .uri("/api/links/{id}", chatId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(response -> eventPublisher.publishEvent(new UpdateMessage(chatId, response)));
    }

    public void sendLink(long chatId, Link link) {
        webClient.post()
            .uri("/api/links/{id}", chatId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(link)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(response -> {
                    eventPublisher.publishEvent(new UpdateMessage(chatId, response));
                    log.info(response);
                },
                error -> log.error("ОШИБКА"));
    }

    public void addUser(long chatId) {
        webClient.put()
            .uri("/api/links/adduser/{chatId}", chatId)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(response -> eventPublisher.publishEvent(new UpdateMessage(chatId, response)));
    }

    public void deleteLink(long chatId, Link link) {
        webClient
            .method(HttpMethod.DELETE)
            .uri("/api/links/{chatId}", chatId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(link)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(response -> eventPublisher.publishEvent(new UpdateMessage(chatId, response)));
    }

}
