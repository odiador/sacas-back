package com.acas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDto {
    private long total;
    private int page;
    private int limit;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrev;
    
    public static PaginationDto of(long total, int page, int limit) {
        int totalPages = (int) Math.ceil((double) total / limit);
        boolean hasNext = page < totalPages;
        boolean hasPrev = page > 1;
        return new PaginationDto(total, page, limit, totalPages, hasNext, hasPrev);
    }
}
