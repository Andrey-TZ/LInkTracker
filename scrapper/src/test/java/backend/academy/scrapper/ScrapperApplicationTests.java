package backend.academy.scrapper;

import backend.academy.scrapper.configs.TestsBeansContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(TestsBeansContainersConfiguration.class)
// @SpringBootTest
class ScrapperApplicationTests {

    @Test
    void contextLoads() {}
}
