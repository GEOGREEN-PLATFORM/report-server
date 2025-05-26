package com.example.report_server.model.geo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность очага на карте")
public class GeoMarkerDTO {

    @NotEmpty
    @Schema(description = "Координаты очага", example = "[1.0, 2.0]")
    private List<Double> coordinate;

    @NotEmpty
    private GeoDetailsDTO details;

    private UUID relatedTaskId;

    private List<List<Double>> coordinates;
}
