package co.apps.ticketing.dto.busfleet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomPagination<T> {

    private int page;

    private int size;

    private int totalPages;     // total halaman

    private long totalElements; // total seluruh data

    private int numberOfElements; // jumlah data di halaman ini

    private List<T> content;
}
