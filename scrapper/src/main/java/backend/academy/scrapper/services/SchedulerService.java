package backend.academy.scrapper.services;

import backend.academy.common.Link;
import backend.academy.scrapper.clients.APIClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.services.link.LinkService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchedulerService {
    private final LinkService linkService;
    private final List<APIClient> apiClients = new ArrayList<>();
    private final int batchSize;

    @Autowired
    public SchedulerService(
            LinkService linkService,
            APIClient gitHubClient,
            APIClient stackOverflowClient,
            ScrapperConfig configuration) {
        this.linkService = linkService;
        apiClients.add(gitHubClient);
        apiClients.add(stackOverflowClient);
        this.batchSize = configuration.batchSize();
    }

    @Scheduled(initialDelay = 1000, fixedRate = 5000)
    public void checkUpdates() {
        int offset = 0;
        Set<Long> users;
        do {
            log.info("Проверка обновлений");
            users = linkService.getUsers(batchSize, offset);
            for (Long user : users) {
                Set<Link> links = linkService.getLinks(user);
                for (Link link : links) {
                    for (APIClient client : apiClients) {
                        if (client.getUpdates(user, link)) {
                            log.info("Успешная проверка");
                            link.resetDate(); // обновляем дату проверки
                            break;
                        }
                    }
                }
            }
            offset += batchSize;
        } while (!users.isEmpty());
    }
}
