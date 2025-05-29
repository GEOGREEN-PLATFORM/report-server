package com.example.report_server.model.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HistoryResponseDTO {

    @NotNull
    private Integer currentPage;

    @NotNull
    private Integer totalItems;

    @NotNull
    private Integer totalPages;

    private List<HistoryDTO> content;
}
