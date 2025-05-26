package com.example.report_server.service.impl;

import com.example.report_server.exception.custom.UnknownReportException;
import com.example.report_server.feignClient.FeignClientEventService;
import com.example.report_server.feignClient.FeignClientFileServer;
import com.example.report_server.feignClient.FeignClientGeoMarkerService;
import com.example.report_server.model.event.EventResponseDTO;
import com.example.report_server.model.event.EventStatusDTO;
import com.example.report_server.model.geo.GeoDetailsDTO;
import com.example.report_server.model.geo.GeoMarkerDTO;
import com.example.report_server.model.geo.GeoResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.example.report_server.model.geo.Density.LOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ReportServiceImplTest {

    @Mock
    private FeignClientGeoMarkerService geoMarkerService;

    @Mock
    private FeignClientEventService eventService;

    @Mock
    private FeignClientFileServer fileServer;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        reportService = new ReportServiceImpl(
                geoMarkerService,
                eventService,
                fileServer
        );
        // подставляем существующие файлы с кириллическими шрифтами для теста
        reportService.boldFontFile = "src/main/resources/fonts/MontserratBold.ttf";
        reportService.regularFontFile = "src/main/resources/fonts/MontserratRegular.ttf";
    }

    @Test
    void getGeneralReport_shouldReturnPdfBytes() {
        GeoResponseDTO geoResponse = new GeoResponseDTO();
        geoResponse.setTotalItems(10);
        geoResponse.setGeoPoints(Collections.emptyList());
        geoResponse.setCurrentPage(0);
        geoResponse.setTotalPages(1);

        EventResponseDTO eventResponse = new EventResponseDTO();
        eventResponse.setTotalItems(5);
        eventResponse.setContent(Collections.emptyList());
        eventResponse.setCurrentPage(0);
        eventResponse.setTotalPages(1);

        EventStatusDTO eventStatusDTO = new EventStatusDTO();
        eventStatusDTO.setCode("Выполнено");
        eventStatusDTO.setDescription("Выполнено");

        EventStatusDTO eventStatusDTO1 = new EventStatusDTO();
        eventStatusDTO.setCode("Химия");
        eventStatusDTO.setDescription("Химия");

        EventStatusDTO eventStatusDTO2 = new EventStatusDTO();
        eventStatusDTO.setCode("Прополка");
        eventStatusDTO.setDescription("Прополка");

        when(geoMarkerService.getAllMarkers(anyString(), anyInt(), anyInt())).thenReturn(geoResponse);
        when(geoMarkerService.getMarkersByStatus(anyString(), anyInt(), anyInt(), anyString())).thenReturn(geoResponse);
        when(geoMarkerService.getMarkersByStatusAndLand(anyString(), anyInt(), anyInt(), any(), any())).thenReturn(geoResponse);
        when(geoMarkerService.getWorkStages(anyString())).thenReturn(List.of("Создан", "Завершено"));
        when(geoMarkerService.getLandTypes(anyString())).thenReturn(List.of("Пашня", "Лес"));

        when(eventService.getAllEvents(anyString(), anyInt(), anyInt())).thenReturn(eventResponse);
        when(eventService.getEventsByStatus(anyString(), anyInt(), anyInt(), anyString())).thenReturn(eventResponse);
        when(eventService.getAllStatuses(anyString())).thenReturn(List.of(eventStatusDTO));
        when(eventService.getAllProblemTypes(anyString())).thenReturn(List.of(eventStatusDTO1));
        when(eventService.getAllEventTypes(anyString())).thenReturn(List.of(eventStatusDTO2));
        when(eventService.getEventsByTypes(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(eventResponse);

        byte[] result = reportService.getGeneralReport("Bearer token");
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void getGeoMarkerReport_shouldReturnPdfBytes() {
        UUID markerId = UUID.randomUUID();

        GeoMarkerDTO marker = new GeoMarkerDTO();
        GeoDetailsDTO details = new GeoDetailsDTO();
        details.setLandType("Лес");
        details.setDensity(LOW);
        details.setWorkStage("Создан");
        details.setCreationDate(OffsetDateTime.now());
        details.setUpdateDate(OffsetDateTime.now());
        details.setContractingOrganization("ООО \"Рога и Копыта\"");
        details.setOwner("Иванов И.И.");
        details.setComment("Комментарий");
        details.setEliminationMethod("Химический");
        details.setProblemAreaType("Поля");
        details.setImages(List.of());
        marker.setDetails(details);

        EventResponseDTO eventResponse = new EventResponseDTO();
        eventResponse.setContent(Collections.emptyList());
        eventResponse.setCurrentPage(0);
        eventResponse.setTotalPages(1);

        when(geoMarkerService.getGeoMarkerById(anyString(), eq(markerId))).thenReturn(marker);
        when(eventService.getEventsByGeoMarker(anyString(), anyInt(), anyInt(), eq(markerId))).thenReturn(eventResponse);

        byte[] result = reportService.getGeoMarkerReport("Bearer token", markerId);
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void getGeneralReport_shouldThrowUnknownReportException_whenFontMissing() {
        reportService.boldFontFile = "nonexistent-bold.ttf";
        reportService.regularFontFile = "nonexistent-regular.ttf";

        assertThatThrownBy(() -> reportService.getGeneralReport("Bearer token"))
                .isInstanceOf(UnknownReportException.class);
    }

    @Test
    void getGeoMarkerReport_shouldThrowUnknownReportException_whenFontMissing() {
        reportService.boldFontFile = "nonexistent-bold.ttf";
        reportService.regularFontFile = "nonexistent-regular.ttf";

        assertThatThrownBy(() -> reportService.getGeoMarkerReport("Bearer token", UUID.randomUUID()))
                .isInstanceOf(UnknownReportException.class);
    }
}
