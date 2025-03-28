package backend.academy.scrapper.repos;

import backend.academy.common.Link;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class LinkRowMapper implements RowMapper<Link> {

    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        Link link = new Link();
        //        link.id(rs.getLong("id"));
        link.url(rs.getString("url"));
        link.date(rs.getTimestamp("creation_date").toLocalDateTime());
        return link;
    }
}
