package backend.academy.bot;

import backend.academy.common.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class UpdateController {

    @PostMapping("/{id}")
    public ResponseEntity<String> receiveUpdates(@PathVariable("id") long id, @RequestBody Update update){
        return ResponseEntity.ok("Updates were received");
    }
}
