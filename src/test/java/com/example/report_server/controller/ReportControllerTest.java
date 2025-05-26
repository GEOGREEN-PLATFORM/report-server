package com.example.report_server.controller;

import com.example.report_server.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@Import(ReportControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ReportService reportService() {
            return Mockito.mock(ReportService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportService reportService;

    private final byte[] dummyPdf = new byte[]{1, 2, 3};

    @Test
    @DisplayName("GET /report/general-report - should return general report PDF")
    void getGeneralReport_shouldReturnPdf() throws Exception {
        when(reportService.getGeneralReport("Bearer test-token")).thenReturn(dummyPdf);

        mockMvc.perform(get("/report/general-report")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"general_report.pdf\""))
                .andExpect(content().bytes(dummyPdf));

        verify(reportService).getGeneralReport("Bearer test-token");
    }

    @Test
    @DisplayName("GET /report/geomarker-report/{id} - should return geomarker report PDF")
    void getGeoMarkerReport_shouldReturnPdf() throws Exception {
        UUID markerId = UUID.randomUUID();

        when(reportService.getGeoMarkerReport("Bearer test-token", markerId)).thenReturn(dummyPdf);

        mockMvc.perform(get("/report/geomarker-report/" + markerId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"geomarker_report.pdf\""))
                .andExpect(content().bytes(dummyPdf));

        verify(reportService).getGeoMarkerReport("Bearer test-token", markerId);
    }
}

