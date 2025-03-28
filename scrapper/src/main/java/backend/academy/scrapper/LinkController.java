package backend.academy.scrapper;

import backend.academy.common.Link;
import backend.academy.scrapper.services.link.LinkService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkService mapLinkService;

    public LinkController(@Autowired LinkService mapLinkService) {
        this.mapLinkService = mapLinkService;
    }

    @PostMapping("/{chatId}")
    public ResponseEntity<String> addLink(@PathVariable("chatId") long chatId, @RequestBody Link link) {
        log.atDebug()
                .setMessage("Добавление ссылки")
                .addKeyValue("user", chatId)
                .addKeyValue("link", link)
                .log();
        // Добавление ссылки в репозиторий пользователя
        mapLinkService.addLink(chatId, link);
        return ResponseEntity.ok("Link was added successfully");
    }

    @PostMapping("/adduser/{chatId}")
    public ResponseEntity<String> addUser(@PathVariable("chatId") long chatId) {
        log.atDebug()
                .setMessage("Создание пользователя")
                .addKeyValue("user", chatId)
                .log();
        // Добавление пользователя в общий репозиторий
        mapLinkService.addUser(chatId);
        return ResponseEntity.status(HttpStatus.CREATED).body("User was added");
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<Set<Link>> getLinks(@PathVariable("chatId") long chatId) {
        log.atDebug()
                .setMessage("Отправка отслеживаемых ссылок")
                .addKeyValue("user", chatId)
                .log();
        Set<Link> links = mapLinkService.getLinks(chatId);
        return ResponseEntity.status(HttpStatus.FOUND).body(links);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> deleteLink(@PathVariable("chatId") long chatId, @RequestBody Link link) {
        log.atDebug()
                .setMessage("Удаление ссылки")
                .addKeyValue("user", chatId)
                .addKeyValue("link", link)
                .log();
        mapLinkService.deleteLink(chatId, link);
        return ResponseEntity.ok("Link has been deleted");
    }
}
