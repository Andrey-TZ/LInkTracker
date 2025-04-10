package backend.academy.bot;

import backend.academy.bot.model.UpdateMessage;
import backend.academy.common.Link;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ScrapperClient {
    private final WebClient webClient;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ScrapperClient(WebClient webClient, ApplicationEventPublisher eventPublisher) {
        this.webClient = webClient;
        this.eventPublisher = eventPublisher;
    }

    public void getLinks(long chatId) {
        webClient
                .get()
                .uri("/api/links/{id}", chatId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ClientException(body))))
                .bodyToMono(new ParameterizedTypeReference<Set<Link>>() {})
                .subscribe(
                        response -> {
                            if (response.isEmpty()) {
                                eventPublisher.publishEvent(
                                        new UpdateMessage(chatId, "У вас нет отслеживаемых ссылок"));
                                return;
                            }
                            String message = "Список отслеживаемых ссылок:\n"
                                    + response.stream()
                                            .map(link -> "- " + link.url())
                                            .collect(Collectors.joining("\n"));
                            eventPublisher.publishEvent(new UpdateMessage(chatId, message));
                        },
                        error -> {
                            log.error("Ошибка при отправке ссылки: {}", error.getMessage());
                            eventPublisher.publishEvent(
                                    new UpdateMessage(chatId, "Не удалось отправить ссылку: \n" + error.getMessage()));
                        });
    }

    public void sendLink(long chatId, Link link) {
        webClient
                .post()
                .uri("/api/links/{id}", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(link)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ClientException(body))))
                .bodyToMono(String.class)
                .subscribe(
                        response -> {
                            eventPublisher.publishEvent(new UpdateMessage(chatId, response));
                            log.info(response);
                        },
                        error -> {
                            log.atError()
                                    .setMessage("Ошибка при отправке сообщения на сервер")
                                    .addKeyValue("Сообщение", error.getMessage())
                                    .log();
                            eventPublisher.publishEvent(
                                    new UpdateMessage(chatId, "Не удалось отправить ссылку: " + error.getMessage()));
                        });
    }

    public void addUser(long chatId) {
        webClient
                .post()
                .uri("/api/links/adduser/{chatId}", chatId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ClientException(body))))
                .bodyToMono(String.class)
                .subscribe(response -> eventPublisher.publishEvent(new UpdateMessage(chatId, response)), error -> {
                    log.error("Ошибка при добавлении пользователя: {}", error.getMessage());
                    eventPublisher.publishEvent(
                            new UpdateMessage(chatId, "Не удалось добавить пользователя: " + error.getMessage()));
                });
    }

    public void deleteLink(long chatId, Link link) {
        webClient
                .method(HttpMethod.DELETE)
                .uri("/api/links/{chatId}", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(link)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ClientException(body))))
                .bodyToMono(String.class)
                .subscribe(
                        response -> eventPublisher.publishEvent(new UpdateMessage(chatId, response)),
                        error -> eventPublisher.publishEvent(
                                new UpdateMessage(chatId, "Не удалось удалить ссылку: " + error.getMessage())));
    }
}
