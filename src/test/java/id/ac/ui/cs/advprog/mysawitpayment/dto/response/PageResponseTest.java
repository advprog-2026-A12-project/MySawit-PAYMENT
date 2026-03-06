package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PageResponseTest {

    @Test
    void testBuilderAndGetter() {

        PageResponse<String> response = PageResponse.<String>builder()
                .content(List.of("A", "B", "C"))
                .page(1)
                .size(3)
                .totalElements(10)
                .totalPages(4)
                .first(false)
                .last(false)
                .build();

        assertEquals(List.of("A", "B", "C"), response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(3, response.getSize());
        assertEquals(10, response.getTotalElements());
        assertEquals(4, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }
}