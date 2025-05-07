package backend.academy.scrapper;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DataBaseMigrator {

    public static void runLiquibaseMigration(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection((connection)));
            Liquibase liquibase = new Liquibase("migrations/db.changelog-master.sql",
                new ClassLoaderResourceAccessor(), database);
            liquibase.update();
        } catch (SQLException | LiquibaseException e) {
            log.atError().addKeyValue("message", e.getMessage())
                .log("Ошибка при запуске миграций");
            throw new RuntimeException(e);
        }
    }
}
