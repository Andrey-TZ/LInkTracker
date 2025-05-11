package backend.academy.scrapper.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        SchedulerConfig scheduler,
        @NotEmpty String botURL,
        @NotEmpty String accessType,
        @Min(1) Integer batchSize,
        @NotEmpty String messageTransport) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    public record SchedulerConfig(@NotEmpty Integer threads) {}
}
