package id.ac.ui.cs.advprog.mysawitpayment.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

final class PageableRequest {

    private static final int MAX_PAGE_SIZE = 100;

    private PageableRequest() {
    }

    static Pageable of(
            int page,
            int size,
            String sort,
            Map<String, String> allowedSortFields,
            String defaultSort
    ) {
        validatePageAndSize(page, size);

        Sort sortSpec = parseSort(
                sort == null || sort.isBlank() ? defaultSort : sort,
                allowedSortFields,
                defaultSort
        );

        return PageRequest.of(page, size, sortSpec);
    }

    static void validatePageAndSize(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }

    static OffsetDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    static OffsetDateTime startOfNextDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private static Sort parseSort(
            String sort,
            Map<String, String> allowedSortFields,
            String defaultSort
    ) {
        String[] parts = sort.split(",");
        String requestedField = parts[0].trim();
        String property = allowedSortFields.get(requestedField);

        if (property == null) {
            Set<String> allowedFields = allowedSortFields.keySet();
            throw new IllegalArgumentException("Sort field must be one of " + allowedFields);
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1 && !parts[1].isBlank()) {
            direction = Sort.Direction.fromString(parts[1].trim());
        } else if (defaultSort != null && sort.equals(defaultSort)) {
            String[] defaultParts = defaultSort.split(",");
            if (defaultParts.length > 1) {
                direction = Sort.Direction.fromString(defaultParts[1].trim());
            }
        }

        return Sort.by(direction, property);
    }
}
