package org.example.forsapidev.Services.Implementation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.forsapidev.Config.PdfConfig;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Services.Interfaces.IAmortizationPdfService;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmortizationPdfService implements IAmortizationPdfService {

    private final PdfConfig pdfConfig;
    private final InsurancePolicyRepository policyRepository;
    private final PremiumPaymentRepository paymentRepository;

    public AmortizationPdfService(PdfConfig pdfConfig,
                                  InsurancePolicyRepository policyRepository,
                                  PremiumPaymentRepository paymentRepository) {
        this.pdfConfig = pdfConfig;
        this.policyRepository = policyRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ByteArrayOutputStream generateAmortizationTablePdf(Long policyId) throws Exception {

        // Get policy
        InsurancePolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found"));

        // Get payments sorted by due date
        List<PremiumPayment> payments = paymentRepository.findByInsurancePolicy(policy)
                .stream()
                .sorted(Comparator.comparing(PremiumPayment::getDueDate))
                .collect(Collectors.toList());

        if (payments.isEmpty()) {
            throw new Exception("No payment schedule found for this policy");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = 750;
                float margin = 50;
                float pageWidth = page.getMediaBox().getWidth();

                // Add header with company logo/name
                yPosition = addCompanyHeader(contentStream, yPosition, margin);

                // Add document title
                yPosition -= 20;
                yPosition = addDocumentTitle(contentStream, yPosition, pageWidth);

                // Add policy information summary
                yPosition -= 30;
                yPosition = addPolicySummary(contentStream, policy, yPosition, margin);

                // Add premium breakdown
                yPosition -= 25;
                yPosition = addPremiumBreakdown(contentStream, policy, yPosition, margin);

                // Add amortization table
                yPosition -= 30;
                yPosition = addAmortizationTable(contentStream, payments, policy, yPosition, margin, pageWidth, document);

                // Add footer
                addFooter(contentStream, page, margin, pageWidth);
            }

            document.save(outputStream);
            System.out.println("✅ Amortization table PDF generated for policy: " + policy.getPolicyNumber());
        }

        return outputStream;
    }

    private float addCompanyHeader(PDPageContentStream contentStream, float yPosition, float margin)
            throws IOException {

        // Company name
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(pdfConfig.getCompanyName());
        contentStream.endText();
        yPosition -= 20;

        // Company address
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(pdfConfig.getCompanyAddress());
        contentStream.endText();
        yPosition -= 12;

        // Contact info
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Phone: " + pdfConfig.getCompanyPhone() + " | Email: " + pdfConfig.getCompanyEmail());
        contentStream.endText();
        yPosition -= 15;

        return yPosition;
    }

    private float addDocumentTitle(PDPageContentStream contentStream, float yPosition, float pageWidth)
            throws IOException {

        String title = "AMORTIZATION SCHEDULE";
        String subtitle = "Payment Plan & Schedule Details";

        // Main title
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                .getStringWidth(title) / 1000 * 16;
        float titleX = (pageWidth - titleWidth) / 2;
        contentStream.newLineAtOffset(titleX, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        yPosition -= 15;

        // Subtitle
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        float subtitleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                .getStringWidth(subtitle) / 1000 * 10;
        float subtitleX = (pageWidth - subtitleWidth) / 2;
        contentStream.newLineAtOffset(subtitleX, yPosition);
        contentStream.showText(subtitle);
        contentStream.endText();
        yPosition -= 5;

        // Horizontal line
        drawLine(contentStream, 50, yPosition, pageWidth - 50, yPosition);
        yPosition -= 15;

        return yPosition;
    }

    private float addPolicySummary(PDPageContentStream contentStream, InsurancePolicy policy,
                                   float yPosition, float margin) throws IOException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Policy Information");
        contentStream.endText();
        yPosition -= 15;

        // Policy details in two columns
        float col1X = margin;
        float col2X = margin + 250;

        yPosition = addInfoLine(contentStream, "Policy Number:", policy.getPolicyNumber(), col1X, yPosition);
        yPosition = addInfoLine(contentStream, "Policy Holder:",
                policy.getUser() != null ? policy.getUser().getUsername() : "N/A", col2X, yPosition + 15);

        yPosition -= 3;
        yPosition = addInfoLine(contentStream, "Coverage Amount:",
                "$" + policy.getCoverageLimit().toString(), col1X, yPosition);
        yPosition = addInfoLine(contentStream, "Payment Frequency:",
                policy.getPaymentFrequency(), col2X, yPosition + 15);

        yPosition -= 3;
        yPosition = addInfoLine(contentStream, "Start Date:",
                policy.getStartDate() != null ? dateFormat.format(policy.getStartDate()) : "Pending", col1X, yPosition);
        yPosition = addInfoLine(contentStream, "End Date:",
                policy.getEndDate() != null ? dateFormat.format(policy.getEndDate()) : "Pending", col2X, yPosition + 15);

        return yPosition;
    }

    private float addPremiumBreakdown(PDPageContentStream contentStream, InsurancePolicy policy,
                                      float yPosition, float margin) throws IOException {

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Premium Breakdown");
        contentStream.endText();
        yPosition -= 15;

        // Premium breakdown table
        float col1X = margin;
        float col2X = margin + 250;

        yPosition = addInfoLine(contentStream, "Pure Premium (Prime Pure):",
                "$" + policy.getPurePremium().toString(), col1X, yPosition);
        yPosition = addInfoLine(contentStream, "Risk Category:",
                policy.getRiskCategory(), col2X, yPosition + 15);

        yPosition -= 3;
        yPosition = addInfoLine(contentStream, "Commercial Premium:",
                "$" + policy.getCommercialPremium().toString(), col1X, yPosition);
        yPosition = addInfoLine(contentStream, "Risk Score:",
                String.format("%.2f", policy.getRiskScore()), col2X, yPosition + 15);

        yPosition -= 3;
        yPosition = addInfoLine(contentStream, "Final Premium (Total):",
                "$" + policy.getFinalPremium().toString(), col1X, yPosition);
        yPosition = addInfoLine(contentStream, "Interest Rate:",
                String.format("%.2f%%", policy.getEffectiveAnnualRate() * 100), col2X, yPosition + 15);

        yPosition -= 3;
        yPosition = addInfoLine(contentStream, "Periodic Payment:",
                "$" + policy.getPeriodicPaymentAmount().toString(), col1X, yPosition);
        yPosition = addInfoLine(contentStream, "Number of Payments:",
                policy.getNumberOfPayments().toString(), col2X, yPosition + 15);

        return yPosition;
    }

    private float addAmortizationTable(PDPageContentStream contentStream, List<PremiumPayment> payments,
                                       InsurancePolicy policy, float yPosition, float margin,
                                       float pageWidth, PDDocument document) throws IOException {

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Payment Schedule");
        contentStream.endText();
        yPosition -= 18;

        // Table headers
        float tableWidth = pageWidth - (2 * margin);
        float[] columnWidths = {50, 90, 90, 90, 90, 90};  // 6 columns
        float cellHeight = 20;

        String[] headers = {"No.", "Due Date", "Payment", "Principal", "Interest", "Balance"};

        // Draw header row
        yPosition = drawTableHeader(contentStream, headers, columnWidths, margin, yPosition, cellHeight);

        // Calculate interest and principal for each payment
        BigDecimal remainingBalance = policy.getFinalPremium();
        double periodicRate = policy.getEffectiveAnnualRate() / getPeriodsPerYear(policy.getPaymentFrequency());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        int paymentNumber = 1;
        for (PremiumPayment payment : payments) {

            // Check if we need a new page
            if (yPosition < 100) {
                contentStream.close();
                PDPage newPage = new PDPage(PDRectangle.A4);
                document.addPage(newPage);
                PDPageContentStream newContentStream = new PDPageContentStream(document, newPage);
                contentStream = newContentStream;
                yPosition = 750;

                // Redraw header on new page
                yPosition = drawTableHeader(contentStream, headers, columnWidths, margin, yPosition, cellHeight);
            }

            // Calculate interest and principal
            BigDecimal interestAmount = remainingBalance.multiply(BigDecimal.valueOf(periodicRate))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal principalAmount = payment.getAmount().subtract(interestAmount)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            // Update balance
            remainingBalance = remainingBalance.subtract(principalAmount);

            // Ensure balance doesn't go negative
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }

            // Prepare row data
            String[] rowData = {
                    String.valueOf(paymentNumber),
                    dateFormat.format(payment.getDueDate()),
                    "$" + payment.getAmount().toString(),
                    "$" + principalAmount.toString(),
                    "$" + interestAmount.toString(),
                    "$" + remainingBalance.toString()
            };

            // Draw row
            yPosition = drawTableRow(contentStream, rowData, columnWidths, margin, yPosition, cellHeight);
            paymentNumber++;
        }

        // Draw totals row
        yPosition -= 5;
        BigDecimal totalPayments = payments.stream()
                .map(PremiumPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = totalPayments.subtract(policy.getFinalPremium());

        String[] totalsRow = {
                "",
                "TOTAL:",
                "$" + totalPayments.toString(),
                "$" + policy.getFinalPremium().toString(),
                "$" + totalInterest.toString(),
                "$0.00"
        };

        yPosition = drawTableRow(contentStream, totalsRow, columnWidths, margin, yPosition, cellHeight, true);

        return yPosition;
    }

    private float drawTableHeader(PDPageContentStream contentStream, String[] headers,
                                  float[] columnWidths, float startX, float startY,
                                  float cellHeight) throws IOException {

        float currentX = startX;

        // Draw header background
        contentStream.setNonStrokingColor(102/255f, 126/255f, 234/255f);  // Blue background
        contentStream.addRect(startX, startY - cellHeight,
                sumArray(columnWidths), cellHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);  // Reset to black

        // Draw header text
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
        for (int i = 0; i < headers.length; i++) {
            // Draw cell border
            drawRectangle(contentStream, currentX, startY - cellHeight, columnWidths[i], cellHeight);

            // Draw text
            contentStream.beginText();
            contentStream.setNonStrokingColor(1, 1, 1);  // White text
            contentStream.newLineAtOffset(currentX + 3, startY - cellHeight + 6);
            contentStream.showText(headers[i]);
            contentStream.endText();
            contentStream.setNonStrokingColor(0, 0, 0);  // Reset to black

            currentX += columnWidths[i];
        }

        return startY - cellHeight;
    }

    private float drawTableRow(PDPageContentStream contentStream, String[] rowData,
                               float[] columnWidths, float startX, float startY,
                               float cellHeight) throws IOException {
        return drawTableRow(contentStream, rowData, columnWidths, startX, startY, cellHeight, false);
    }

    private float drawTableRow(PDPageContentStream contentStream, String[] rowData,
                               float[] columnWidths, float startX, float startY,
                               float cellHeight, boolean isBold) throws IOException {

        float currentX = startX;

        // Set font
        PDType1Font font = isBold ?
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD) :
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        contentStream.setFont(font, 8);

        for (int i = 0; i < rowData.length; i++) {
            // Draw cell border
            drawRectangle(contentStream, currentX, startY - cellHeight, columnWidths[i], cellHeight);

            // Draw text
            if (rowData[i] != null && !rowData[i].isEmpty()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(currentX + 3, startY - cellHeight + 6);
                contentStream.showText(rowData[i]);
                contentStream.endText();
            }

            currentX += columnWidths[i];
        }

        return startY - cellHeight;
    }

    private float addInfoLine(PDPageContentStream contentStream, String label, String value,
                              float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
        contentStream.newLineAtOffset(x + 120, y);
        contentStream.showText(value);
        contentStream.endText();

        return y - 13;
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
        float footerY = 40;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.newLineAtOffset(margin, footerY);
        contentStream.showText("Generated on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.newLineAtOffset(margin, footerY - 10);
        contentStream.showText("This is a computer-generated document. For questions, contact " + pdfConfig.getCompanyEmail());
        contentStream.endText();
    }

    private float sumArray(float[] array) {
        float sum = 0;
        for (float value : array) {
            sum += value;
        }
        return sum;
    }

    private int getPeriodsPerYear(String frequency) {
        switch (frequency.toUpperCase()) {
            case "MONTHLY": return 12;
            case "QUARTERLY": return 4;
            case "SEMI_ANNUAL": return 2;
            case "ANNUAL": return 1;
            default: return 12;
        }
    }
}