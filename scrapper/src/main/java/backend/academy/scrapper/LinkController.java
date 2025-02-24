package backend.academy.scrapper;

import backend.academy.common.Link;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/links")
public class LinkController {

    @PostMapping("/{chatId}")
    public ResponseEntity<String> addLink(@PathVariable("chatId") long chatId, @RequestBody Link link) {
        // Добавление ссылки в репозиторий пользователя
        return ResponseEntity.ok("Added successful");
    }

    @PutMapping("/adduser/{chatId}")
    public ResponseEntity<String> addUser(@PathVariable("chatId") long chatId) {
        log.info("put запрос {}", chatId);
        // Добавление пользователя в общий репозиторий
        return ResponseEntity.status(HttpStatus.CREATED).body("User was added");
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<List<Link>> getLinks(@PathVariable("chatId") long chatId) {
        List<Link> links = new ArrayList<>();
        // Получение всех отслеживаемых ссылок пользователя
        return ResponseEntity.status(HttpStatus.FOUND).body(links);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> deleteLink(@PathVariable("chatId") long chatId, @RequestBody Link link) {
        // удаление ссылки
        return ResponseEntity.ok("Link has been deleted");
    }
}
