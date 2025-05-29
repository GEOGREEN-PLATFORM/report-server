package com.example.report_server.model.geo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность ответа сервиса с очагами")
public class GeoResponseDTO {

    @NotEmpty
    private List<GeoMarkerDTO> geoPoints;

    @NotNull
    private Integer currentPage;

    @NotNull
    private Integer totalItems;

    @NotNull
    private Integer totalPages;
}
