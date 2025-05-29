package com.example.report_server.model.geo;

import com.example.report_server.model.image.ImageDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import static com.example.report_server.util.DateUtil.ISO_8601_DATE_TIME_MILLIS_PATTERN;
import static com.example.report_server.util.DateUtil.UTC;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность детальной информации об очаге")
public class GeoDetailsDTO {

    @NotEmpty
    @Schema(example = "Требует заполнения сотрудником")
    private String owner;

    @NotEmpty
    @Schema(example = "Требует заполнения сотрудником")
    private String contractingOrganization;

    @NotEmpty
    @Schema(description = "Статус очага", example = "Создано")
    private String workStage;

    @Schema(description = "Лист айди фотографий борщевика")
    private List<ImageDTO> images;

    @NotNull
    @Size(max = 50)
    @Schema(description = "Тип экологической проблемы", example = "Борщевик")
    private String problemAreaType;

    @Size(max = 256)
    @Schema(description = "Комментарий оператора", example = "тут много борщевика")
    private String comment;

    private Double square;

    @Schema(description = "Тип земли")
    private String landType;

    @Schema(description = "Метод обработки")
    private String eliminationMethod;

    private Density density;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = ISO_8601_DATE_TIME_MILLIS_PATTERN,
            timezone = UTC
    )
    private OffsetDateTime creationDate;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = ISO_8601_DATE_TIME_MILLIS_PATTERN,
            timezone = UTC
    )
    private OffsetDateTime updateDate;
}
