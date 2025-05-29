package com.example.report_server.model.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseDTO {
    @NotNull
    private Integer currentPage;

    @NotNull
    private Integer totalItems;

    @NotNull
    private Integer totalPages;

    private List<EventDTO> content;
}
