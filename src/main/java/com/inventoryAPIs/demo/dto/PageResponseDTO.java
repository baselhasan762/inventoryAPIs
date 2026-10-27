package com.inventoryAPIs.demo.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Stable JSON shape for paged results. Spring's {@link Page} is not serialized directly
 * because its JSON layout is an internal detail that can change between versions.
 */
@Getter
@Builder
public class PageResponseDTO<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public static <E, T> PageResponseDTO<T> from(Page<E> page, Function<E, T> mapper) {
        return PageResponseDTO.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
