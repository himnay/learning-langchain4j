package com.org.llm.validation;

import com.org.llm.exception.SqlValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SqlValidatorTest {

    private final SqlValidator validator = new SqlValidator();

    @Test
    void sanitizeStripsFencesAndTrailingSemicolon() {
        assertThat(validator.sanitize("```sql\nSELECT 1 FROM text2sql_orders;\n```"))
                .isEqualTo("SELECT 1 FROM text2sql_orders");
    }

    @Test
    void sanitizeRejectsBlankSql() {
        assertThatExceptionOfType(SqlValidationException.class).isThrownBy(() -> validator.sanitize("  "));
    }

    @Test
    void validateReadOnlyAcceptsSelectOnAllowedTables() {
        String sql = "SELECT o.id FROM text2sql_orders o JOIN text2sql_customers c ON c.id = o.customer_id";
        assertThat(validator.validateReadOnly(sql)).isEqualTo(sql);
    }

    @Test
    void validateReadOnlyRejectsMutatingStatements() {
        assertThatExceptionOfType(SqlValidationException.class)
                .isThrownBy(() -> validator.validateReadOnly("DELETE FROM text2sql_orders"));
    }

    @Test
    void validateReadOnlyRejectsForbiddenKeywordInsideSelect() {
        assertThatExceptionOfType(SqlValidationException.class)
                .isThrownBy(() -> validator.validateReadOnly("SELECT 1 FROM text2sql_orders; DROP TABLE x"));
    }

    @Test
    void validateReadOnlyRejectsTablesOutsideAllowList() {
        assertThatExceptionOfType(SqlValidationException.class)
                .isThrownBy(() -> validator.validateReadOnly("SELECT * FROM api_keys"));
    }

    @Test
    void enforceLimitAppendsLimitWhenMissing() {
        assertThat(validator.enforceLimit("SELECT * FROM text2sql_orders", 50))
                .isEqualTo("SELECT * FROM text2sql_orders LIMIT 50");
    }

    @Test
    void enforceLimitKeepsExistingLimitAndCountQueries() {
        assertThat(validator.enforceLimit("SELECT * FROM text2sql_orders LIMIT 5", 50))
                .isEqualTo("SELECT * FROM text2sql_orders LIMIT 5");
        assertThat(validator.enforceLimit("SELECT COUNT(*) FROM text2sql_orders", 50))
                .isEqualTo("SELECT COUNT(*) FROM text2sql_orders");
    }

    @Test
    void prepareRunsFullPipeline() {
        assertThat(validator.prepare("```sql\nSELECT * FROM text2sql_products;\n```", 25))
                .isEqualTo("SELECT * FROM text2sql_products LIMIT 25");
    }
}
