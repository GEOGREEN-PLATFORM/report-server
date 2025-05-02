package com.example.report_server.service;

import java.io.IOException;

public interface ReportService {
    byte[] getGeneralReport(String token) throws IOException;
}
