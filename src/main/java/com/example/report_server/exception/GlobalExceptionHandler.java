package com.example.report_server.exception;

import com.example.report_server.exception.custom.UnknownReportException;
import com.example.report_server.model.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler({ UnknownReportException.class })
    public ResponseEntity<ResponseDTO> catchTransactionSystemException(UnknownReportException e) {
        logger.error("Произошла ошибка: {}", e.getMessage(), e);
        return new ResponseEntity<>(new ResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()), HttpStatus.BAD_REQUEST
        );
    }
}
