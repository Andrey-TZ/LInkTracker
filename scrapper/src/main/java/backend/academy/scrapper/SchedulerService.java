package backend.academy.scrapper;

import backend.academy.common.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import backend.academy.scrapper.clients.APIClient;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOerFlowClient;
import backend.academy.scrapper.data.LinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchedulerService {
    private final LinkRepository repo;
    private final List<APIClient> apiClients = new ArrayList<>();

    @Autowired
    public SchedulerService(LinkRepository repo, GitHubClient gitHubClient, StackOerFlowClient stackOerFlowClient) {
        this.repo = repo;
        apiClients.add(gitHubClient);
        apiClients.add(stackOerFlowClient);
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkUpdates() {
        log.info("Проверка обновлений");
        Set<Long> users = repo.getUsers();
        for (Long user : users) {
            Set<Link> links = repo.getLinks(user);
            for (Link link : links) {
                for (APIClient client : apiClients) {
                    if (client.getUpdates(user, link)) {
                        log.info("Успешная проверка");
                        link.setDate(); // обновляем дату проверки
                        break;
                    }
                }
            }
        }
    }
}
