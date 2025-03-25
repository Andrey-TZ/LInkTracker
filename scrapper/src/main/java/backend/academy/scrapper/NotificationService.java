package backend.academy.scrapper;

import backend.academy.common.Update;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void sendUpdate(long chatId, Update update);
}
