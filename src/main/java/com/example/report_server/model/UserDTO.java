package com.example.report_server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сущность для хранения ФИО пользвоателя")
public class UserDTO implements Serializable {

    @Schema(description = "Айди пользователя", example = "12f0887f-6b1d-4ee0-aba9-9e006bc4745e")
    private UUID id;

    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    private String patronymic;
}
