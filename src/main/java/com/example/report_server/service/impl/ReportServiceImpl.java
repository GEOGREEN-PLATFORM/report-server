package com.example.report_server.service.impl;

import com.example.report_server.feignClient.FeignClientGeoMarkerService;
import com.example.report_server.model.geo.GeoMarkerDTO;
import com.example.report_server.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @Autowired
    private final FeignClientGeoMarkerService feignClientGeoMarkerService;

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Override
    public byte[] getGeneralReport(String token) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // 2. Подготавливаем поток для записи
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 3. Настройка шрифтов
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);

                // 4. Заголовок отчета
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700); // Позиция (x, y)
                contentStream.showText("Report");
                contentStream.endText();

                // 5. Основные данные
//                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
//                contentStream.beginText();
//                contentStream.newLineAtOffset(50, 650);
//                contentStream.showText("Всего очагов: " + stats.getTotalOutbreaks());
//                contentStream.newLineAtOffset(0, -20);
//                contentStream.showText("Активные: " + stats.getActiveOutbreaks());
//                contentStream.newLineAtOffset(0, -20);
//                contentStream.showText("Устранено: " + stats.getResolvedOutbreaks());
//                contentStream.endText();
//
//                // 6. Таблица с распределением по типам земель
//                contentStream.beginText();
//                contentStream.newLineAtOffset(50, 550);
//                contentStream.showText("Распределение по типам земель:");
//                contentStream.endText();
//
//                float y = 530;
//                for (Map.Entry<String, Integer> entry : stats.getOutbreaksByLandType().entrySet()) {
//                    contentStream.beginText();
//                    contentStream.newLineAtOffset(50, y);
//                    contentStream.showText(entry.getKey() + ": " + entry.getValue());
//                    contentStream.endText();
//                    y -= 20;
//                }
            }

            // 7. Сохраняем PDF в ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private List<GeoMarkerDTO> getAllGeoMarkers(String token) {
        return feignClientGeoMarkerService.getAllMarkers(token);
    }

    private List<String> getWorkStages(String token) {
        return feignClientGeoMarkerService.getWorkStages(token);
    }
}
