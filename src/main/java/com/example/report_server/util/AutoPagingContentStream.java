package com.example.report_server.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import java.io.IOException;

@Getter
@Setter
public class AutoPagingContentStream implements AutoCloseable {

    private final PDDocument document;
    private PDPageContentStream currentStream;
    private final PDType0Font boldFont;
    private final PDType0Font regularFont;
    private int lastY = 700;
    private final int lineSpacing = 20;
    private final int bottomMargin = 50;

    public AutoPagingContentStream(PDDocument document, PDType0Font boldFont, PDType0Font regularFont) throws IOException {
        this.document = document;
        this.boldFont = boldFont;
        this.regularFont = regularFont;
        newPage();
    }

    private void newPage() throws IOException {
        if (currentStream != null) {
            currentStream.close();
        }
        PDPage page = new PDPage();
        document.addPage(page);
        currentStream = new PDPageContentStream(document, page);
        lastY = 700;
    }

    public void updateY(int newY) throws IOException {
        if (newY < bottomMargin) {
            newPage();
        }
        else {
            this.lastY = newY;
        }
    }

    public void checkAvailableSpace(int space) throws IOException {
        if (lastY - space <= bottomMargin) {
            newPage();
        }
    }

    public void addText(PDType0Font font, String text, int fontSize, int x, int r, int g, int b) throws IOException {
        int requiredSpace = fontSize + lineSpacing;
        if (lastY - requiredSpace < bottomMargin) {
            newPage();
        }

        float[] colorComponents = {r / 255.0f, g / 255.0f, b / 255.0f};
        PDColor color = new PDColor(colorComponents, PDDeviceRGB.INSTANCE);

        currentStream.setNonStrokingColor(color);
        currentStream.setFont(font, fontSize);

        currentStream.beginText();
        currentStream.newLineAtOffset(x, lastY);
        currentStream.showText(text);
        currentStream.endText();

        updateY(lastY - lineSpacing);
    }

    public void addCustomText(PDPageContentStream contentStream,
                         PDType0Font font,
                         String text,
                         int fontSize,
                         int posX,
                         int posY,
                         int r, int g, int b) throws IOException {
        float[] colorComponents = rgbToFloat(r, g, b);
        PDColor color = new PDColor(colorComponents, PDDeviceRGB.INSTANCE);

        contentStream.setNonStrokingColor(color);
        contentStream.setFont(font, fontSize);

        contentStream.beginText();
        contentStream.newLineAtOffset(posX, posY);
        contentStream.showText(text);
        contentStream.endText();
    }

    @Override
    public void close() throws IOException {
        if (currentStream != null) {
            currentStream.close();
        }
    }

    private float[] rgbToFloat(int r, int g, int b) {
        return new float[] {
                r / 255.0f,
                g / 255.0f,
                b / 255.0f
        };
    }
}
