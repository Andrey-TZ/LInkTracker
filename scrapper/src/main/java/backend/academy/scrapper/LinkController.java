package backend.academy.scrapper;

import backend.academy.common.Link;
import backend.academy.scrapper.data.LinkRepository;
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
    private final LinkRepository mapLinkRepository;

    public LinkController(@Autowired LinkRepository mapLinkRepository) {
        this.mapLinkRepository = mapLinkRepository;
    }

    @PostMapping("/{chatId}")
    public ResponseEntity<String> addLink(@PathVariable("chatId") long chatId, @RequestBody Link link) {
        // Добавление ссылки в репозиторий пользователя
        mapLinkRepository.addLink(chatId, link);
        return ResponseEntity.ok("Link was added successfully");
    }

    @PostMapping("/adduser/{chatId}")
    public ResponseEntity<String> addUser(@PathVariable("chatId") long chatId) {
        // Добавление пользователя в общий репозиторий
        mapLinkRepository.addUser(chatId);
        return ResponseEntity.status(HttpStatus.CREATED).body("User was added");
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<Set<Link>> getLinks(@PathVariable("chatId") long chatId) {
        Set<Link> links = mapLinkRepository.getLinks(chatId);
        return ResponseEntity.status(HttpStatus.FOUND).body(links);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> deleteLink(@PathVariable("chatId") long chatId, @RequestBody Link link) {
        mapLinkRepository.deleteLink(chatId, link);
        return ResponseEntity.ok("Link has been deleted");
    }
}
