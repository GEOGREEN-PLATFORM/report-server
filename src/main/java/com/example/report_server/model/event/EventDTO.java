package com.example.report_server.model.event;


import com.example.report_server.model.UserDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;


@Data
@AllArgsConstructor
public class EventDTO {

    @NotNull
    private UUID id;

    @NotNull
    private UUID geoPointId;

    @NotNull
    private Instant startDate;

    private Instant endDate;

    @NotNull
    private Instant lastUpdateDate;

    private String statusCode;

    @NotNull
    private String eventType;

    @NotNull
    private String problemAreaType;

    @NotNull
    private String description;

    @NotNull
    private String name;

    private UserDTO operator;

    private UUID operatorId;

    private UserDTO author;

    private String operatorFullText;

}
