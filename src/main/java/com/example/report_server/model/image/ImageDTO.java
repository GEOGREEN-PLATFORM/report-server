package com.example.report_server.model.image;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность изображения")
public class ImageDTO {
    @NotNull
    private UUID previewImageId;

    @NotNull
    private UUID fullImageId;
}
