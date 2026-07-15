package com.tracelens.common;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious,
        String sortBy,
        String sortDirection
) {

    public static <T> PageResponse<T> from(
            Page<T> page,
            String sortBy,
            String sortDirection
    ) {

        return new PageResponse<>(
                List.copyOf(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious(),
                sortBy,
                sortDirection
        );
    }
}