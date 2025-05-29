package com.example.report_server.model.event;

import com.example.report_server.model.UserDTO;
import com.example.report_server.model.image.ImageDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HistoryDTO {

    @NotNull
    private UUID id;

    @NotNull
    private UUID eventId;

    @NotNull
    private Instant recordDate;

    @NotNull
    private String recordType;

    @NotNull
    private String description;

    private List<ImageDTO> photos;

    private Instant createDate;

    private UserDTO operator;
}
