package com.org.llm.tool;

import com.org.llm.model.Contact;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Not {@code @Transactional}: that annotation makes Spring wrap this bean in a CGLIB subclass
 * proxy, and LangChain4j's {@code AiServices} tool scanner reflects on the proxy's own declared
 * methods — which don't carry the proxied class's {@code @Tool} annotations — so a proxied tool
 * bean fails to register at all ("does not have any methods annotated with @Tool"). A single
 * read-only {@code JdbcTemplate} query doesn't need an explicit transaction boundary anyway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContactsTool {

    private final JdbcTemplate jdbcTemplate;

    @Tool("Find contacts in a given city")
    public List<Contact> findContactsByCity(@P("city to search") String city) {
        log.info("Finding contacts in city: " + city);
        String sql = "SELECT name, email, city FROM contacts WHERE city = ?";

        RowMapper<Contact> rowMapper = (rs, rowNum) ->
                new Contact(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("city")
                );

        return jdbcTemplate.query(sql, rowMapper, city);
    }

    @Tool("Formats a list of contacts into CSV with headers: Name, Email, City")
    public String formatAsCsv(@P("contacts to format") List<Contact> contacts) {
        StringBuilder sb = new StringBuilder("Name,Email,City\n");
        for (Contact c : contacts) {
            sb.append(c.getName())
                    .append(",")
                    .append(c.getEmail())
                    .append(",")
                    .append(c.getCity())
                    .append("\n");
        }
        return sb.toString();
    }
}
