package backend.academy.scrapper.repos;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class JPARepositoryEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return "ORM".equals(context.getEnvironment().getProperty("app.access-type"));
    }
}
