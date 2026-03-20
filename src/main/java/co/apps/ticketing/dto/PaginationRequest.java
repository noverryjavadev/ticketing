package co.apps.ticketing.dto;

public record PaginationRequest(int page, int size, String orderBy, String sortDirection) {
}
