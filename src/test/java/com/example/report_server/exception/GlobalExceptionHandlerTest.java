package com.example.report_server.exception;

import com.example.report_server.exception.custom.UnknownReportException;
import com.example.report_server.model.ResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("should handle UnknownReportException and return ResponseDTO with BAD_REQUEST")
    void catchTransactionSystemException_shouldReturnBadRequest() {
        // given
        String errorMessage = "Unknown report type";
        UnknownReportException exception = new UnknownReportException(errorMessage);

        // when
        ResponseEntity<ResponseDTO> response = handler.catchTransactionSystemException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Произошла неизвестная ошибка при формировании отчета: " + errorMessage);
    }
}
