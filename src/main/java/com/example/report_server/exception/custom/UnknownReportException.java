package com.example.report_server.exception.custom;

public class UnknownReportException extends RuntimeException {
    public UnknownReportException(String message) {
        super("Произошла неизвестная ошибка при формировании отчета: " + message);
    }
}
