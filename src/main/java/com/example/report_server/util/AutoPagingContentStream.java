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
        if (text == null)
            text = "";
        contentStream.showText(text);
        contentStream.endText();
    }

    public void addCustomLineText(PDPageContentStream contentStream,
                              PDType0Font font,
                              String text,
                              int fontSize,
                              int posX,
                              int posY,
                              int r, int g, int b) throws IOException {
        contentStream.setNonStrokingColor(new PDColor(rgbToFloat(r, g, b), PDDeviceRGB.INSTANCE));
        contentStream.setFont(font, fontSize);

        final float maxWidth = 500f;
        final float lineHeight = fontSize * 1.2f;
        final float scale = fontSize / 1000f;

        contentStream.beginText();
        contentStream.newLineAtOffset(posX, posY);

        StringBuilder currentLine = new StringBuilder();
        float currentWidth = 0;

        for (String word : text.split(" ")) {
            float wordWidth = font.getStringWidth(word) * scale;
            float potentialWidth = currentWidth + (currentLine.length() > 0 ? font.getStringWidth(" ") * scale : 0) + wordWidth;

            if (potentialWidth > maxWidth && currentLine.length() > 0) {
                contentStream.showText(currentLine.toString());
                contentStream.newLineAtOffset(0, -lineHeight);
                currentLine.setLength(0);
                currentWidth = 0;
            }

            if (currentLine.length() > 0) {
                currentLine.append(" ");
                currentWidth += font.getStringWidth(" ") * scale;
            }

            currentLine.append(word);
            currentWidth += wordWidth;
        }

        if (currentLine.length() > 0) {
            contentStream.showText(currentLine.toString());
            updateY(lastY - lineSpacing);
        }

        contentStream.endText();
        updateY(lastY - lineSpacing);
    }

    public void drawHorizontalLine(float startX) throws IOException {
        checkAvailableSpace(lineSpacing * 2);

        float pageWidth = 595;
        float endX = pageWidth - 50;
        float yPos = lastY - 10;
        float lineWidth = 0.5f;

        currentStream.setStrokingColor(new PDColor(new float[]{0.8f, 0.8f, 0.8f}, PDDeviceRGB.INSTANCE));
        currentStream.setLineWidth(lineWidth);
        currentStream.moveTo(startX, yPos);
        currentStream.lineTo(endX, yPos);
        currentStream.stroke();

        updateY((int)(yPos - lineSpacing * 2));
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
