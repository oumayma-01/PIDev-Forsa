package org.example.forsapidev.Services.Implementation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.forsapidev.Config.PdfConfig;
import org.example.forsapidev.DTO.ComparisonResultDTO;
import org.example.forsapidev.DTO.InsuranceProductComparisonDTO;
import org.example.forsapidev.Services.Interfaces.IComparisonPdfService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ComparisonPdfService implements IComparisonPdfService {

    private final PdfConfig pdfConfig;

    public ComparisonPdfService(PdfConfig pdfConfig) {
        this.pdfConfig = pdfConfig;
    }

    @Override
    public ByteArrayOutputStream generateComparisonPdf(ComparisonResultDTO comparison) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = 750;
                float margin = 50;
                float pageWidth = page.getMediaBox().getWidth();

                // Add header
                yPosition = addHeader(contentStream, yPosition, margin, pageWidth);

                // Add title
                yPosition = addTitle(contentStream, "INSURANCE PRODUCTS COMPARISON", yPosition, pageWidth);
                yPosition -= 20;

                // Add summary
                yPosition = addSummary(contentStream, comparison.getComparisonSummary(), yPosition, margin, pageWidth);
                yPosition -= 30;

                // Add comparison table
                yPosition = addComparisonTable(contentStream, comparison, yPosition, margin, pageWidth);

                // Add footer
                addFooter(contentStream, page, margin, pageWidth);
            }

            document.save(outputStream);
            System.out.println("✅ Comparison PDF generated successfully");
        }

        return outputStream;
    }

    private float addHeader(PDPageContentStream contentStream, float yPosition,
                            float margin, float pageWidth) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(pdfConfig.getCompanyName());
        contentStream.endText();
        yPosition -= 25;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(pdfConfig.getCompanyAddress());
        contentStream.endText();
        yPosition -= 20;

        return yPosition;
    }

    private float addTitle(PDPageContentStream contentStream, String title,
                           float yPosition, float pageWidth) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                .getStringWidth(title) / 1000 * 16;
        float titleX = (pageWidth - titleWidth) / 2;
        contentStream.newLineAtOffset(titleX, yPosition);
        contentStream.showText(title);
        contentStream.endText();

        yPosition -= 10;
        drawLine(contentStream, 50, yPosition, pageWidth - 50, yPosition);

        return yPosition - 20;
    }

    private float addSummary(PDPageContentStream contentStream, String summary,
                             float yPosition, float margin, float pageWidth) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Recommendation:");
        contentStream.endText();
        yPosition -= 15;

        // Wrap text
        String[] words = summary.split(" ");
        StringBuilder line = new StringBuilder();
        float maxWidth = pageWidth - (2 * margin);

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

        for (String word : words) {
            String testLine = line + word + " ";
            float lineWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                    .getStringWidth(testLine) / 1000 * 10;

            if (lineWidth > maxWidth) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(line.toString().trim());
                contentStream.endText();
                yPosition -= 12;
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
        }

        if (line.length() > 0) {
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(line.toString().trim());
            contentStream.endText();
            yPosition -= 12;
        }

        return yPosition;
    }

    private float drawTableRow(PDPageContentStream contentStream, String[] cells,
                               float startX, float startY, float colWidth, float rowHeight,
                               boolean isHeader) throws IOException {
        // Draw cell borders
        for (int i = 0; i < cells.length; i++) {
            float x = startX + (i * colWidth);
            drawRectangle(contentStream, x, startY - rowHeight, colWidth, rowHeight);
        }

        // Draw text
        PDType1Font font = isHeader ?
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD) :
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float fontSize = isHeader ? 10 : 9;

        for (int i = 0; i < cells.length; i++) {
            float x = startX + (i * colWidth) + 5;
            float y = startY - (rowHeight / 2) - 3;

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(cells[i] != null ? cells[i] : "N/A");
            contentStream.endText();
        }

        return startY - rowHeight;
    }

    private float addComparisonTable(PDPageContentStream contentStream, ComparisonResultDTO comparison,
                                     float yPosition, float margin, float pageWidth) throws IOException {
        float tableWidth = pageWidth - (2 * margin);
        float colWidth = tableWidth / (comparison.getProducts().size() + 1);
        float rowHeight = 25;

        List<InsuranceProductComparisonDTO> products = comparison.getProducts();

        // Table headers
        String[] headers = new String[products.size() + 1];
        headers[0] = "Feature";
        for (int i = 0; i < products.size(); i++) {
            headers[i + 1] = products.get(i).getProductName();
        }

        // Draw header row
        yPosition = drawTableRow(contentStream, headers, margin, yPosition, colWidth, rowHeight, true);

        // Draw data rows - FIXED
        String[][] rows = {
                createRow("Policy Type", products, p -> p.getPolicyType()),
                createRow("Premium", products, p -> "$" + p.getPremiumAmount().toString()),
                createRow("Coverage", products, p -> "$" + p.getCoverageLimit().toString()),
                createRow("Duration", products, p -> p.getDurationMonths() + " months"),
                createRow("Monthly Cost", products, p -> p.getCostPerMonth() != null ? "$" + p.getCostPerMonth().toString() : "N/A"),
                createRow("Coverage/$", products, p -> p.getCoveragePerDollar() != null ? "$" + String.format("%.2f", p.getCoveragePerDollar()) : "N/A"),
                createRow("Value Rating", products, p -> p.getValueRating() != null ? p.getValueRating() : "N/A")
        };

        for (String[] row : rows) {
            yPosition = drawTableRow(contentStream, row, margin, yPosition, colWidth, rowHeight, false);
        }

        // Highlight best values
        yPosition -= 20;
        yPosition = addBestValueIndicators(contentStream, comparison, margin, yPosition);

        return yPosition;
    }

    private float addBestValueIndicators(PDPageContentStream contentStream, ComparisonResultDTO comparison,
                                         float margin, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Best Options:");
        contentStream.endText();
        yPosition -= 15;

        String bestValue = findProductName(comparison, comparison.getBestValueProductId());
        String lowestPremium = findProductName(comparison, comparison.getLowestPremiumProductId());
        String highestCoverage = findProductName(comparison, comparison.getHighestCoverageProductId());

        yPosition = addIndicator(contentStream, "⭐ Best Value:", bestValue, margin, yPosition);
        yPosition = addIndicator(contentStream, "💰 Lowest Premium:", lowestPremium, margin, yPosition);
        yPosition = addIndicator(contentStream, "🛡️ Highest Coverage:", highestCoverage, margin, yPosition);

        return yPosition;
    }

    private float addIndicator(PDPageContentStream contentStream, String label, String value,
                               float margin, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(label + " " + value);
        contentStream.endText();
        return yPosition - 15;
    }

    private String findProductName(ComparisonResultDTO comparison, Long productId) {
        return comparison.getProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .map(InsuranceProductComparisonDTO::getProductName)
                .findFirst()
                .orElse("N/A");
    }

    private String[] createRow(String label, List<InsuranceProductComparisonDTO> products,
                               java.util.function.Function<InsuranceProductComparisonDTO, String> valueExtractor) {
        String[] row = new String[products.size() + 1];
        row[0] = label;
        for (int i = 0; i < products.size(); i++) {
            try {
                row[i + 1] = valueExtractor.apply(products.get(i));
            } catch (Exception e) {
                row[i + 1] = "N/A";
            }
        }
        return row;
    }

    private void drawRectangle(PDPageContentStream contentStream, float x, float y,
                               float width, float height) throws IOException {
        contentStream.addRect(x, y, width, height);
        contentStream.stroke();
    }

    private void drawLine(PDPageContentStream contentStream, float x1, float y, float x2, float y2)
            throws IOException {
        contentStream.moveTo(x1, y);
        contentStream.lineTo(x2, y2);
        contentStream.stroke();
    }

    private void addFooter(PDPageContentStream contentStream, PDPage page, float margin,
                           float pageWidth) throws IOException {
        float footerY = 50;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
        contentStream.newLineAtOffset(margin, footerY);
        contentStream.showText("Generated on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        contentStream.endText();
    }

    private String[] addToArray(String[] array, String element) {
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }
}