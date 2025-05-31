package backend.academy.scrapper.config;

import backend.academy.common.Link;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisConfiguration {
    @Value("${spring.cache.redis.time-to-live}")
    private int ttl;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        JavaType type = mapper.getTypeFactory().constructCollectionType(Set.class, Link.class);

        Jackson2JsonRedisSerializer<Set<Link>> serializer = new Jackson2JsonRedisSerializer<>(mapper, type);

        //        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
        //            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        //        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        //            .allowIfSubType(Object.class) // можно сузить под свои классы
        //            .build();

        //        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);//
        // 👈 важно для сохранения информации о типах (в т.ч. Set vs List)

        //        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper,
        // Object.class);
        //        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisSerializationContext.SerializationPair<Set<Link>> pair =
                RedisSerializationContext.SerializationPair.fromSerializer(serializer);

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(ttl))
                .enableTimeToIdle()
                .disableCachingNullValues()
                .serializeValuesWith(pair);
        //            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
        //                new GenericJackson2JsonRedisSerializer()))
        //            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new
        // StringRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        return template;
    }
}
