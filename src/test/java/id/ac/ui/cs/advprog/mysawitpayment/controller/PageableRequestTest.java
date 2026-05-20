package id.ac.ui.cs.advprog.mysawitpayment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageableRequestTest {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "amountSawitDollar", "amountSawitDollar"
    );

    @Test
    void ofShouldUseDefaultSortWhenSortIsNull() {
        Pageable pageable = PageableRequest.of(1, 25, null, SORT_FIELDS, "createdAt,asc");

        assertEquals(1, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
        assertEquals("createdAt: ASC", pageable.getSort().toString());
    }

    @Test
    void ofShouldUseDefaultSortWhenSortIsBlank() {
        Pageable pageable = PageableRequest.of(0, 10, " ", SORT_FIELDS, "createdAt,desc");

        assertEquals("createdAt: DESC", pageable.getSort().toString());
    }

    @Test
    void ofShouldDefaultDirectionToDescWhenDirectionMissing() {
        Pageable pageable = PageableRequest.of(0, 10, "amountSawitDollar", SORT_FIELDS, "createdAt,desc");

        assertEquals("amountSawitDollar: DESC", pageable.getSort().toString());
    }

    @Test
    void ofShouldKeepDescWhenSortMatchesDefaultWithoutDirection() {
        Pageable pageable = PageableRequest.of(0, 10, "createdAt", SORT_FIELDS, "createdAt");

        assertEquals("createdAt: DESC", pageable.getSort().toString());
    }

    @Test
    void ofShouldRejectBlankDefaultDirection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PageableRequest.of(0, 10, "createdAt, ", SORT_FIELDS, "createdAt, ")
        );
    }

    @Test
    void ofShouldRejectUnknownSortField() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PageableRequest.of(0, 10, "userName,asc", SORT_FIELDS, "createdAt,desc")
        );

        assertTrue(exception.getMessage().startsWith("Sort field must be one of "));
        assertTrue(exception.getMessage().contains("createdAt"));
        assertTrue(exception.getMessage().contains("amountSawitDollar"));
    }

    @Test
    void ofShouldRejectInvalidDirection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PageableRequest.of(0, 10, "createdAt,sideways", SORT_FIELDS, "createdAt,desc")
        );
    }

    @Test
    void dateHelpersShouldConvertToUtcBoundaries() {
        LocalDate date = LocalDate.of(2026, 5, 20);

        assertEquals(
                OffsetDateTime.of(2026, 5, 20, 0, 0, 0, 0, ZoneOffset.UTC),
                PageableRequest.startOfDay(date)
        );
        assertEquals(
                OffsetDateTime.of(2026, 5, 21, 0, 0, 0, 0, ZoneOffset.UTC),
                PageableRequest.startOfNextDay(date)
        );
    }
}
