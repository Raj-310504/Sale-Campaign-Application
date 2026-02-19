package com.example.SaleCampaign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PaginationResponse<T> {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<T> data;
}
