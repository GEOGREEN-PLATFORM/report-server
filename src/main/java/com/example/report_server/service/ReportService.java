package com.example.report_server.service;

import java.util.UUID;

public interface ReportService {
    byte[] getGeneralReport(String token);

    byte[] getGeoMarkerReport(String token, UUID id);
}
