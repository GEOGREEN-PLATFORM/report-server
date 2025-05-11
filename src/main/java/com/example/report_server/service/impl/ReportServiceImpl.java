package com.example.report_server.service.impl;

import com.example.report_server.exception.custom.UnknownReportException;
import com.example.report_server.feignClient.FeignClientEventService;
import com.example.report_server.feignClient.FeignClientFileServer;
import com.example.report_server.feignClient.FeignClientGeoMarkerService;
import com.example.report_server.model.event.EventResponseDTO;
import com.example.report_server.model.event.EventStatusDTO;
import com.example.report_server.model.geo.GeoMarkerDTO;
import com.example.report_server.model.geo.GeoResponseDTO;
import com.example.report_server.model.image.ImageDTO;
import com.example.report_server.service.ReportService;
import com.example.report_server.util.AutoPagingContentStream;
import com.example.report_server.util.Colors;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @Autowired
    private final FeignClientGeoMarkerService feignClientGeoMarkerService;

    @Autowired
    private final FeignClientEventService feignClientEventService;

    @Autowired
    private final FeignClientFileServer feignClientFileServer;

    @Value("${bold.font.file}")
    private String boldFontFile;

    @Value("${regular.font.file}")
    private String regularFontFile;

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final int x = 50;
    private final int headerSize = 16;
    private final int regular1Size = 14;
    private final int regular2Size = 10;
    private int pageSize = 100;

    @Override
    public byte[] getGeneralReport(String token) {
        try (PDDocument document = new PDDocument()) {
            PDType0Font bold = PDType0Font.load(document, new File(boldFontFile));
            PDType0Font regular = PDType0Font.load(document, new File(regularFontFile));

            try (AutoPagingContentStream autoStream = new AutoPagingContentStream(document, bold, regular)) {

                addTitle(autoStream,
                        bold, headerSize, "Общий статистический отчет по очагам",
                        regular, regular2Size, "Отчет собран за весь период.");

                autoStream.addText(bold, "Краткая сводка по основным показателям:",
                        regular1Size, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
                autoStream.setLastY(autoStream.getLastY() - autoStream.getLineSpacing());

                GeoResponseDTO allMarkers = feignClientGeoMarkerService.getAllMarkers(token, 0, pageSize);
                addDigitWithLabel(autoStream, bold, headerSize, allMarkers.getTotalItems(), Colors.YELLOW, regular, regular1Size, "очагов было создано", x);
                autoStream.setLastY(autoStream.getLastY() + 3 * autoStream.getLineSpacing());

                GeoResponseDTO markersByFinalStatus = feignClientGeoMarkerService.getMarkersByStatus(token, 0, pageSize, "Завершено");
                addDigitWithLabel(autoStream, bold, headerSize, markersByFinalStatus.getTotalItems(), Colors.GREEN, regular, regular1Size, "очагов было обработано", x + 200);


                double processedArea = calculateSquare(feignClientGeoMarkerService.getMarkersByStatusAndLand(token, 0, pageSize, "Завершено", null), token, "Завершено");
                addDigitWithLabel(autoStream, bold, headerSize, (int) processedArea, Colors.GREEN, regular, regular1Size, "площади обработано", x);
                autoStream.setLastY(autoStream.getLastY() + 3 * autoStream.getLineSpacing());

                double allArea = calculateSquare(feignClientGeoMarkerService.getMarkersByStatusAndLand(token, 0, pageSize, null, null), token, null);
                addDigitWithLabel(autoStream, bold, headerSize, (int) (allArea - processedArea), Colors.RED, regular, regular1Size, "площадь распространения", x + 200);

                EventResponseDTO allEvents = feignClientEventService.getAllEvents(token, 0, pageSize);
                addDigitWithLabel(autoStream, bold, headerSize, allEvents.getTotalItems(), Colors.YELLOW, regular, regular1Size, "всего мероприятий", x);
                autoStream.setLastY(autoStream.getLastY() + 3 * autoStream.getLineSpacing());

                EventResponseDTO finishedEvents = feignClientEventService.getEventsByStatus(token, 0, pageSize, "Выполнено");
                addDigitWithLabel(autoStream, bold, headerSize, finishedEvents.getTotalItems(), Colors.GREEN, regular, regular1Size, "выполнено мероприятий", x + 200);


                autoStream.setLastY(autoStream.getLastY() - 2 * autoStream.getLineSpacing());
                autoStream.checkAvailableSpace(220 + 2 * autoStream.getLineSpacing());
                autoStream.addText(bold, "Распределение очагов по текущим статусам:", regular1Size, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);

                paintPieChartByStatus(autoStream, allMarkers.getTotalItems(), token, regular, "geo");
                autoStream.setLastY(autoStream.getLastY() - (220 + autoStream.getLineSpacing()));

                autoStream.addText(bold, "Распределение очагов по типу обрабатываемых земель и статусам:",
                        regular1Size, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
                autoStream.setLastY(autoStream.getLastY() - autoStream.getLineSpacing());

                drawTable(autoStream, x, token, regular, "geo");

                autoStream.addText(bold, "Распределение мероприятий по текущим статусам:", regular1Size, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
                paintPieChartByStatus(autoStream, allEvents.getTotalItems(), token, regular, "event");
                autoStream.setLastY(autoStream.getLastY() - (220 + autoStream.getLineSpacing()));

                autoStream.addText(bold, "Распределение мероприятий по виду и типу проблемы:",
                        regular1Size, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
                autoStream.setLastY(autoStream.getLastY() - autoStream.getLineSpacing());

                drawTable(autoStream, x, token, regular, "event");

                autoStream.addText(bold, "Организации, работающие над очагами:",
                        regular1Size, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);

                drawList(autoStream, getContractingOrganizations(token), regular);
            }

            return saveDocumentToBytes(document);
        } catch (IOException e) {
            throw new UnknownReportException(e.getMessage());
        }
    }

    @Override
    public byte[] getGeoMarkerReport(String token, UUID id) {
        try (PDDocument document = new PDDocument()) {
            PDType0Font bold = PDType0Font.load(document, new File(boldFontFile));
            PDType0Font regular = PDType0Font.load(document, new File(regularFontFile));

            try (AutoPagingContentStream autoStream = new AutoPagingContentStream(document, bold, regular)) {

                addTitle(autoStream,
                        bold, headerSize, "Детальный отчет по очагу",
                        regular, regular2Size, "Ссылка на очаг: http://сылка на очаг.ру");

                drawHotbed(id, token, autoStream, regular);
            }

            return saveDocumentToBytes(document);
        } catch (IOException e) {
            throw new UnknownReportException(e.getMessage());
        }
    }

    private void drawHotbed(UUID id, String token, AutoPagingContentStream autoStream, PDType0Font font) throws IOException {
        GeoMarkerDTO geoMarker = feignClientGeoMarkerService.getGeoMarkerById(token, id);
        float bulletWidth = 100f;
        float bulletHeight = 20f;
        float bulletSpace = 15f;
        int fontSize = 12;

        drawBullet(autoStream, Colors.VERYLIGHTGREEN, Colors.LIGHTGREEN, font, geoMarker.getDetails().getProblemAreaType(), x, autoStream.getLastY(), bulletHeight, bulletWidth, fontSize);
        drawBullet(autoStream, Colors.VERYLIGHTRED, Colors.LIGHTRED, font, geoMarker.getDetails().getWorkStage(), x + bulletWidth + bulletSpace, autoStream.getLastY(), bulletHeight, bulletWidth, fontSize);
        drawBullet(autoStream, Colors.VERYLIGHTPURPLE, Colors.LIGHTPURPLE, font, "Плотность: " + geoMarker.getDetails().getDensity().toString(), x + (bulletWidth + bulletSpace) * 2, autoStream.getLastY(), bulletHeight, bulletWidth + 50, fontSize);
        autoStream.updateY((int) (autoStream.getLastY() - bulletHeight - bulletSpace));
        drawBullet(autoStream, Colors.VERYLIGHTYELLOW, Colors.LIGHTYELLOW, font, "Дата создания: " + geoMarker.getDetails().getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), x, autoStream.getLastY(), bulletHeight - 8, bulletWidth + 50, 8);
        drawBullet(autoStream, Colors.VERYLIGHTORANGE, Colors.LIGHTORANGE, font, "Дата обновления: " + geoMarker.getDetails().getUpdateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), x + bulletWidth + 50 + bulletSpace, autoStream.getLastY(), bulletHeight - 8, bulletWidth + 50, 8);
        autoStream.updateY((int) (autoStream.getLastY() - bulletHeight - bulletSpace));

        PDPageContentStream contentStream = autoStream.getCurrentStream();
        autoStream.addCustomText(
                contentStream, font, "Владелец - " + geoMarker.getDetails().getOwner() + ", Организация - " + geoMarker.getDetails().getContractingOrganization(), 10,
                x, autoStream.getLastY(),
                Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]
        );
        autoStream.updateY((int) (autoStream.getLastY() - bulletSpace));

        drawImages(autoStream, geoMarker.getDetails().getImages(), x, 100, 100, 5);

        contentStream = autoStream.getCurrentStream();
        autoStream.addCustomLineText(
                contentStream, font, "Комментарий: " + geoMarker.getDetails().getComment(), fontSize,
                x, autoStream.getLastY(),
                Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]
        );

        drawBullet(autoStream, Colors.VERYLIGHTGREEN, Colors.LIGHTGREEN, font, "Тип земли: " + geoMarker.getDetails().getLandType(), x, autoStream.getLastY(), bulletHeight - 8, bulletWidth + 70, 8);
        drawBullet(autoStream, Colors.VERYLIGHTORANGE, Colors.LIGHTORANGE, font, "Метод обработки: " + geoMarker.getDetails().getEliminationMethod(), x + bulletWidth + 70 + bulletSpace, autoStream.getLastY(), bulletHeight - 8, bulletWidth + 50, 8);
        autoStream.updateY((int) (autoStream.getLastY() - bulletHeight - bulletSpace));

    }

    private void drawImages(AutoPagingContentStream autoStream, List<ImageDTO> images, int x, float width, float height, int countPerLine) throws IOException {
        float imageSpace = 10f;

        PDPageContentStream contentStream = autoStream.getCurrentStream();
        int count = 0;
        int row = 0;
        for (ImageDTO image: images) {
            PDImageXObject photo = PDImageXObject.createFromByteArray(
                    autoStream.getDocument(),
                    feignClientFileServer.downloadImage(image.getFullImageId()),
                    "photo"
            );
            contentStream.drawImage(photo, (float) x + (height + imageSpace) * (count), autoStream.getLastY() - height - (height + imageSpace) * row, width, height);
            count += 1;
            if (count == countPerLine) {
                row += 1;
                count = 0;
            }
        }
        autoStream.updateY((int) (autoStream.getLastY() - (height + imageSpace) * (row + 1) - imageSpace));
    }

    private void drawList(AutoPagingContentStream autoStream, List<String> items, PDType0Font font) throws IOException {
        autoStream.checkAvailableSpace(items.size() * 30 + 50);

        float itemHeight = 25f;
        float itemWidth = 500f;
        float startY = autoStream.getLastY();

        for (int i = 0; i < items.size(); i++) {
            String org = items.get(i) != null ? items.get(i) : "Не указана";

            boolean isEven = i % 2 == 0;

            drawBullet(autoStream, isEven ? new int[]{242, 242, 242} : new int[]{255, 255, 255}, Colors.LIGHTBLUE, font, org, x, startY, itemHeight, itemWidth, 12);

            startY -= itemHeight;
        }

        autoStream.setLastY((int) (startY - 20));
    }

    private void drawBullet(AutoPagingContentStream autoStream, int[] bgColor, int[] bulletColor, PDType0Font textFont, String text, float x, float y, float height, float width, int fontSize) throws IOException {
        PDPageContentStream contentStream = autoStream.getCurrentStream();

        PDColor bg = new PDColor(new float[]{
                bgColor[0]/255f,
                bgColor[1]/255f,
                bgColor[2]/255f
        },
                PDDeviceRGB.INSTANCE
        );

        PDColor bullet = new PDColor(new float[]{
                bulletColor[0]/255f,
                bulletColor[1]/255f,
                bulletColor[2]/255f
        },
                PDDeviceRGB.INSTANCE
        );

        contentStream.setLineWidth(5f);
        contentStream.setLineJoinStyle(1);
        contentStream.setStrokingColor(bg);
        contentStream.setNonStrokingColor(bg);
        contentStream.addRect(x, y - height, width, height);
        contentStream.fillAndStroke();

        contentStream.setLineWidth(3f);
        contentStream.setLineJoinStyle(1);
        contentStream.setNonStrokingColor(bullet);
        contentStream.addRect(x + 5, y - height + (height - 10) / 2, 10, 10);
        contentStream.fillAndStroke();

        autoStream.addCustomText(
                contentStream, textFont, text, fontSize,
                (int)(x + 20), (int)(y - height / 2 - 4),
                Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]
        );
    }

    private void drawTable(AutoPagingContentStream autoStream, float x, String token, PDType0Font font, String type) throws IOException {
        List<String> statuses;
        Map<String, Map<String, Integer>> data;

        if (Objects.equals(type, "geo")) {
            statuses = feignClientGeoMarkerService.getWorkStages(token);
            data = prepareDataGeoByStatusTable(token);
        }
        else {
            statuses = getStringFromTypesDto(feignClientEventService.getAllProblemTypes(token));
            data = prepareDataEventByTypesTable(token, statuses);
        }

        float colWidth = (Objects.equals(type, "geo") ? 100f : 110f), rowHeight = 25f;
        autoStream.checkAvailableSpace((int) (rowHeight * data.size() + autoStream.getLineSpacing()));
        float y = autoStream.getLastY();

        // Заголовки
        drawCell(autoStream, x, y, colWidth * (Objects.equals(type, "geo") ? 2 : 1), rowHeight, Objects.equals(type, "geo") ? "Тип земли" : "Тип мероприятия", true, font);
        for (int i = 0; i < statuses.size(); i++) {
            drawCell(autoStream, x + colWidth * (i + (Objects.equals(type, "geo") ? 2 : 1)), y, colWidth, rowHeight, statuses.get(i), true, font);
        }

        // Данные
        int row = 0;
        for (var entry : data.entrySet()) {
            y -= rowHeight;
            drawCell(autoStream, x, y, colWidth * (Objects.equals(type, "geo") ? 2 : 1), rowHeight, entry.getKey(), row%2 == 0, font);

            for (int i = 0; i < statuses.size(); i++) {
                int count = entry.getValue().getOrDefault(statuses.get(i), 0);
                drawCell(autoStream, x + colWidth * (i + (Objects.equals(type, "geo") ? 2 : 1)), y, colWidth, rowHeight, String.valueOf(count), row%2 == 0, font);
            }
            row++;
        }

        autoStream.updateY(autoStream.getLastY() - ((statuses.size() + 5) * (int) rowHeight));
    }

    private void drawCell(AutoPagingContentStream autoStream, float x, float y, float w, float h, String text, boolean isEven, PDType0Font font) throws IOException {
        PDPageContentStream contentStream = autoStream.getCurrentStream();

        contentStream.setNonStrokingColor(new PDColor(isEven ? new float[]{0.95f,0.95f,0.95f} : new float[]{1,1,1}, PDDeviceRGB.INSTANCE));
        contentStream.addRect(x, y, w, h);
        contentStream.fill();

        autoStream.addCustomText(contentStream, font, text, 12, (int) (x + 5), (int) (y + h/2 - 4), Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
    }

    private void paintPieChartByStatus(AutoPagingContentStream autoStream, Integer totalItems, String token, PDType0Font font, String type) throws IOException {
        Map<String, Integer> data = new LinkedHashMap<>();
        int[][] colors = {Colors.RED, Colors.YELLOW, Colors.GREEN};


        if (Objects.equals(type, "geo")) {
            List<String> statuses = feignClientGeoMarkerService.getWorkStages(token);

            for (String status: statuses) {
                GeoResponseDTO response = feignClientGeoMarkerService.getMarkersByStatus(token, 0, pageSize, status);
                data.put(status, response.getTotalItems() * 100 / totalItems);
            }

        }
        else if (Objects.equals(type, "event")) {
            List<EventStatusDTO> statuses = feignClientEventService.getAllStatuses(token);
            for (EventStatusDTO status: statuses) {
                EventResponseDTO response = feignClientEventService.getEventsByStatus(token, 0, pageSize, status.getCode());
                data.put(status.getCode(), response.getTotalItems() * 100 / totalItems);
            }
            colors = new int[][]{Colors.RED, Colors.YELLOW, Colors.ORANGE, Colors.GRAY, Colors.PURPLE, Colors.GREEN};
        }

        PDPageContentStream contentStream = autoStream.getCurrentStream();
        float radius = 100;
        float centerX = x + radius;
        float centerY = autoStream.getLastY() - radius;
        float startAngle = 0;

        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            if (entry.getValue() == 0) {
                colorIndex++;
                continue;
            }

            float extent = 360 * ((float) entry.getValue() / 100);

            int[] rgb = colors[colorIndex % colors.length];
            contentStream.setNonStrokingColor(new PDColor(new float[]{rgb[0]/255f, rgb[1]/255f, rgb[2]/255f}, PDDeviceRGB.INSTANCE));

            drawPieSegment(contentStream, centerX, centerY, radius, startAngle, extent);

            startAngle += extent;
            colorIndex++;
        }

        float legendX = centerX + radius + 70;
        float legendY = centerY + radius - autoStream.getLineSpacing();
        drawLegend(autoStream, legendX, legendY, data, colors, font);
    }

    private void drawPieSegment(PDPageContentStream contentStream, float centerX, float centerY, float radius,
                                float startAngle, float extent) throws IOException {

        contentStream.moveTo(centerX, centerY);
        int segments = (int) Math.ceil(extent);

        for (int i = 0; i <= segments; i++) {
            float angle = startAngle + (extent * i / segments);
            float x = centerX + radius * (float) Math.cos(Math.toRadians(angle));
            float y = centerY + radius * (float) Math.sin(Math.toRadians(angle));

            if (i == 0) {
                contentStream.lineTo(x, y);
            } else {
                contentStream.lineTo(x, y);
            }
        }

        contentStream.lineTo(centerX, centerY);
        contentStream.closePath();
        contentStream.fill();
    }

    private void drawLegend(AutoPagingContentStream autoStream,
                            float startX, float startY,
                            Map<String, Integer> data,
                            int[][] colors, PDType0Font font) throws IOException {
        float boxSize = 15;
        float spacing = 25;
        float textOffset = 20;

        PDPageContentStream contentStream = autoStream.getCurrentStream();

        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            int[] rgb = colors[colorIndex % colors.length];
            contentStream.setNonStrokingColor(
                    new PDColor(new float[]{rgb[0]/255f, rgb[1]/255f, rgb[2]/255f},
                            PDDeviceRGB.INSTANCE
                    ));
            contentStream.addRect(startX, startY, boxSize, boxSize);
            contentStream.fill();

            autoStream.addCustomText(contentStream, font, String.format("%s (%d%%)", entry.getKey(), entry.getValue()), regular1Size, (int) (startX + textOffset), (int) (startY + boxSize/2 - 4), Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);

            startY -= spacing;
            colorIndex++;
        }
    }

    private void addDigitWithLabel(AutoPagingContentStream stream,
                                   PDType0Font digitFont, Integer digitSize, Integer digit, int[] digitColor,
                                   PDType0Font labelFont, Integer labelSize, String labelText, int x) throws IOException {
        stream.addText(digitFont, String.valueOf(digit), digitSize, x, digitColor[0], digitColor[1], digitColor[2]);
        stream.addText(labelFont, labelText, labelSize, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
        stream.setLastY(stream.getLastY() - stream.getLineSpacing());
    }

    private void addTitle(AutoPagingContentStream stream,
                          PDType0Font titleFont, Integer titleSize, String titleText,
                          PDType0Font subtitleFont, Integer subtitleSize, String subtitleText) throws IOException {
        stream.addText(titleFont, titleText, titleSize, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
        if (subtitleText != null) {
            stream.addText(subtitleFont, subtitleText, subtitleSize, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
        }
        stream.addText(subtitleFont, "Дата создания отчета: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), subtitleSize, x, Colors.GRAY[0], Colors.GRAY[1], Colors.GRAY[2]);
        stream.setLastY(stream.getLastY() - stream.getLineSpacing());
    }

    private PDPage createPage(PDDocument document) {
        PDPage page = new PDPage();
        document.addPage(page);
        return page;
    }

    private byte[] saveDocumentToBytes(PDDocument document) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        return outputStream.toByteArray();
    }

    private float[] rgbToFloat(int r, int g, int b) {
        return new float[] {
                r / 255.0f,
                g / 255.0f,
                b / 255.0f
        };
    }

    private Map<String, Map<String, Integer>> prepareDataGeoByStatusTable(String token) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        List<String> landTypes = feignClientGeoMarkerService.getLandTypes(token);
        List<String> statuses = feignClientGeoMarkerService.getWorkStages(token);

        for (String landType: landTypes) {
            Map<String, Integer> countByStatus = new HashMap<>();
            for (String status: statuses) {
                GeoResponseDTO res = feignClientGeoMarkerService.getMarkersByStatusAndLand(token, 0, pageSize, status, landType);
                countByStatus.put(status, res.getTotalItems());
            }
            result.put(landType, countByStatus);
        }
        return result;
    }

    private Map<String, Map<String, Integer>> prepareDataEventByTypesTable(String token, List<String> problemTypes) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        List<String> eventTypes = getStringFromTypesDto(feignClientEventService.getAllEventTypes(token));

        for (String eventType: eventTypes) {
            Map<String, Integer> countByProblem = new HashMap<>();
            for (String problem: problemTypes) {
                EventResponseDTO res = feignClientEventService.getEventsByTypes(token, 0, pageSize, eventType, problem);
                countByProblem.put(problem, res.getTotalItems());
            }
            result.put(eventType, countByProblem);
        }
        return result;
    }

    private List<String> getStringFromTypesDto(List<EventStatusDTO> list) {
        List<String> result = new ArrayList<>();
        for (EventStatusDTO item: list) {
            result.add(item.getCode());
        }
        return result;
    }

    private double calculateSquare(GeoResponseDTO markers, String token, String workStage) {
        double result = 0;
        int curPage = markers.getCurrentPage();
        int countPages = markers.getTotalPages();
        while (curPage < countPages) {
            for (GeoMarkerDTO marker : markers.getGeoPoints()) {
                try {
                    result += marker.getDetails().getSquare();

                }
                catch (NullPointerException e) {
                    result += 0;
                }
            }
            curPage += 1;
            markers = feignClientGeoMarkerService.getMarkersByStatusAndLand(token, curPage, pageSize, workStage, null);
        }
        return result;
    }

    private List<String> getContractingOrganizations(String token) {
        Set<String> organizations = new HashSet<>();
        GeoResponseDTO response = feignClientGeoMarkerService.getAllMarkers(token, 0, pageSize);

        int curPage = 0;
        int countPages = response.getTotalPages();

        while (curPage < countPages) {
            for (GeoMarkerDTO marker : response.getGeoPoints()) {
                try {
                    organizations.add(marker.getDetails().getContractingOrganization());

                }
                catch (NullPointerException e) {
                    continue;
                }
            }
            curPage += 1;
            response = feignClientGeoMarkerService.getAllMarkers(token, curPage, pageSize);
        }

        return new ArrayList<>(organizations);
    }
}
