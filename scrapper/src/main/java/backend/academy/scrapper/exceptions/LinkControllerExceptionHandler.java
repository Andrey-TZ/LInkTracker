package backend.academy.scrapper.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class LinkControllerExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        log.atWarn()
                .setMessage("Пользователь не найден")
                .addKeyValue("exception", ex.getClass())
                .addKeyValue("message", ex.getMessage())
                .log();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.atWarn()
                .setMessage("Пользователь уже создан")
                .addKeyValue("exception", ex.getClass())
                .addKeyValue("message", ex.getMessage())
                .log();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(LinkAlreadyExistsException.class)
    public ResponseEntity<String> handleLinkAlreadyExists(LinkAlreadyExistsException ex) {
        log.atWarn()
                .setMessage("Ссылка уже отслеживается пользователем")
                .addKeyValue("exception", ex.getClass())
                .addKeyValue("message", ex.getMessage())
                .log();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<String> handleLinkNotFound(LinkNotFoundException ex) {
        log.atWarn()
                .setMessage("Ссылка не найдена")
                .addKeyValue("exception", ex.getClass())
                .addKeyValue("message", ex.getMessage())
                .log();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(ValidationException ex) {
        log.atWarn()
                .setMessage("Ошибка валидации")
                .addKeyValue("exception", ex.getClass())
                .addKeyValue("message", ex.getMessage())
                .log();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.atError()
                .setMessage("Неожидаемое исключение")
                .addKeyValue("exception", ex.getClass())
                .addKeyValue("message", ex.getMessage())
                .log();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Внутренняя ошибка сервера: " + ex.getMessage());
    }
}
