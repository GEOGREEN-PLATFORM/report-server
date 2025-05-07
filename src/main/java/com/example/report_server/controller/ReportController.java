package com.example.report_server.controller;

import com.example.report_server.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.report_server.util.AuthorizationStringUtil.AUTHORIZATION;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@SecurityRequirement(name = AUTHORIZATION)
@Tag(name = "Генерация отчетов", description = "Позволяет создавать отчеты по выбранным критериям")
public class ReportController {

    @Autowired
    private ReportService reportService;

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @GetMapping("/general-report")
    @Operation(
            summary = "Общий отчет",
            description = "Автоматически создает общий отчет"
    )
    public ResponseEntity<byte[]> getGeneralReport(@RequestHeader("Authorization") String token) {
        logger.info("Получен запрос на генерацию общего отчета");
        byte[] pdfBytes = reportService.getGeneralReport(token);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"hogweed_report.pdf\"")
                .body(pdfBytes);
    }
}
