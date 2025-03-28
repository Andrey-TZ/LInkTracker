// package backend.academy.scrapper;
//
// import org.springframework.boot.jdbc.DataSourceBuilder;
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.jdbc.core.simple.JdbcClient;
// import javax.sql.DataSource;
//// import org.testcontainers.kafka.KafkaContainer;
//// import org.testcontainers.utility.DockerImageName;
//
//// isolated from the "bot" module's containers!
// @TestConfiguration
// public class TestsBeansContainersConfiguration {
//
////    @Bean
////    @RestartScope
////    @ServiceConnection(name = "redis")
////    GenericContainer<?> redisContainer() {
////        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
////    }
//
////    @Bean(initMethod = "start", destroyMethod = "stop")
////    @RestartScope
////    @ServiceConnection
////    PostgreSQLContainer<?> postgresContainer() {
////        return new PostgreSQLContainer<>("postgres:17-alpine")
////            .withExposedPorts(5432)
////            .withDatabaseName("local")
////            .withUsername("postgres")
////            .withPassword("test");
////    }
//
//    @Bean
//    public DataSource dataSource() {
//        return DataSourceBuilder.create()
//            .url("jdbc:postgresql://localhost:5432/scrapper_test")
//            .username("test")
//            .password("test")
//            .driverClassName("org.postgresql.Driver")
//            .build();
//    }
//
//    @Bean
//    public JdbcClient jdbcClient(DataSource dataSource) {
//        return JdbcClient.create(dataSource);
//    }
//
////    @Bean
////    @RestartScope
////    @ServiceConnection
////    KafkaContainer kafkaContainer() {
////        return new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);
////    }
// }
