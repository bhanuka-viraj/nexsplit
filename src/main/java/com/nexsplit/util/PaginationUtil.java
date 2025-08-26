package com.nexsplit.util;

import com.nexsplit.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Utility class for creating consistent paginated responses
 */
public class PaginationUtil {

    /**
     * Create a paginated response from a Spring Data Page
     */
    public static <T> PaginatedResponse<T> createPaginatedResponse(Page<T> page, String endpoint) {
        return PaginatedResponse.<T>builder()
                .data(page.getContent())
                .pagination(createPaginationInfo(page, endpoint))
                .build();
    }

    /**
     * Create a paginated response from a list with pagination info
     */
    public static <T> PaginatedResponse<T> createPaginatedResponse(List<T> data, int page, int size, long totalElements,
            String endpoint) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PaginatedResponse.<T>builder()
                .data(data)
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .hasNext(page < totalPages - 1)
                        .hasPrevious(page > 0)
                        .nextPageUrl(page < totalPages - 1 ? buildPageUrl(endpoint, page + 1, size) : null)
                        .previousPageUrl(page > 0 ? buildPageUrl(endpoint, page - 1, size) : null)
                        .build())
                .build();
    }

    private static PaginatedResponse.PaginationInfo createPaginationInfo(Page<?> page, String endpoint) {
        return PaginatedResponse.PaginationInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .nextPageUrl(page.hasNext() ? buildPageUrl(endpoint, page.getNumber() + 1, page.getSize()) : null)
                .previousPageUrl(
                        page.hasPrevious() ? buildPageUrl(endpoint, page.getNumber() - 1, page.getSize()) : null)
                .build();
    }

    private static String buildPageUrl(String endpoint, int page, int size) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("page", page)
                .replaceQueryParam("size", size)
                .build()
                .toUriString();
    }
}
