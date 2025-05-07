package com.example.report_server.model.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventStatusDTO {

    @NotNull
    private Integer id;

    @NotNull
    private String code;

    private String description;

    private boolean isDefault = false;
}
