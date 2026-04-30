package org.example.forsapidev.Services.Implementation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.forsapidev.Config.PdfConfig;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Services.Interfaces.IPolicyPdfService;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PolicyPdfService implements IPolicyPdfService {

    private final InsurancePolicyRepository policyRepository;
    private final PdfConfig pdfConfig;

    // Brand Colors
    private final Color NAVY = new Color(15, 23, 42);
    private final Color TEAL = new Color(16, 185, 129);
    private final Color LIGHT_GRAY = new Color(241, 245, 249);
    private final Color TEXT_GRAY = new Color(100, 116, 139);

    public PolicyPdfService(InsurancePolicyRepository policyRepository, PdfConfig pdfConfig) {
        this.policyRepository = policyRepository;
        this.pdfConfig = pdfConfig;
    }

    @Override
    public ByteArrayOutputStream generatePolicyPdf(Long policyId) throws Exception {
        InsurancePolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found with ID: " + policyId));
        return generatePolicyPdf(policy);
    }

    @Override
    public ByteArrayOutputStream generatePolicyPdf(InsurancePolicy policy) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float margin = 50;
                float yPos = pageHeight - margin;

                // 1. Header with Background Bar
                contentStream.setNonStrokingColor(NAVY);
                contentStream.addRect(0, pageHeight - 120, pageWidth, 120);
                contentStream.fill();

                // Company Name (White)
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.newLineAtOffset(margin, pageHeight - 60);
                contentStream.showText(pdfConfig.getCompanyName().toUpperCase());
                contentStream.endText();

                // Contract Label
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.setNonStrokingColor(TEAL);
                contentStream.newLineAtOffset(margin, pageHeight - 85);
                contentStream.showText("OFFICIAL INSURANCE CONTRACT");
                contentStream.endText();

                yPos = pageHeight - 150;

                // 2. Policy Header Info (Top Right)
                contentStream.setNonStrokingColor(NAVY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(pageWidth - 200, pageHeight - 50);
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.showText("POLICY #: " + policy.getPolicyNumber());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                contentStream.newLineAtOffset(pageWidth - 200, pageHeight - 65);
                String issueDate = policy.getStartDate() != null ? new SimpleDateFormat("dd MMM yyyy").format(policy.getStartDate()) : "N/A";
                contentStream.showText("Issued on: " + issueDate);
                contentStream.endText();

                // 3. Client & Product Section
                yPos = drawSectionHeader(contentStream, "CONTRACT PARTIES", yPos, margin, pageWidth);
                
                float col1 = margin;
                float col2 = pageWidth / 2 + 20;
                float startY = yPos;

                // Left: Client Info
                contentStream.setNonStrokingColor(TEXT_GRAY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(col1, yPos);
                contentStream.showText("INSURED PARTY");
                contentStream.endText();
                yPos -= 15;

                contentStream.setNonStrokingColor(NAVY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(col1, yPos);
                contentStream.showText(policy.getUser() != null ? policy.getUser().getUsername() : "N/A");
                contentStream.endText();
                yPos -= 15;

                contentStream.setNonStrokingColor(TEXT_GRAY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(col1, yPos);
                contentStream.showText(policy.getUser() != null ? policy.getUser().getEmail() : "");
                contentStream.endText();

                // Right: Product Info
                yPos = startY;
                contentStream.setNonStrokingColor(TEXT_GRAY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(col2, yPos);
                contentStream.showText("INSURANCE PRODUCT");
                contentStream.endText();
                yPos -= 15;

                contentStream.setNonStrokingColor(NAVY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(col2, yPos);
                contentStream.showText(policy.getInsuranceProduct() != null ? policy.getInsuranceProduct().getProductName() : "Custom Plan");
                contentStream.endText();
                yPos -= 15;

                contentStream.setNonStrokingColor(TEXT_GRAY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(col2, yPos);
                contentStream.showText("Type: " + policy.getPolicyType());
                contentStream.endText();

                yPos -= 50;

                // 4. Coverage Table
                yPos = drawSectionHeader(contentStream, "COVERAGE & PREMIUMS", yPos, margin, pageWidth);
                
                // Draw a simple box for coverage
                contentStream.setNonStrokingColor(LIGHT_GRAY);
                contentStream.addRect(margin, yPos - 80, pageWidth - (margin * 2), 80);
                contentStream.fill();

                float tableY = yPos - 25;
                drawRow(contentStream, "Total Coverage Limit", policy.getCoverageLimit() + " TND", tableY, margin, pageWidth);
                drawRow(contentStream, "Premium Amount", policy.getPremiumAmount() + " TND", tableY - 20, margin, pageWidth);
                drawRow(contentStream, "Payment Frequency", policy.getPaymentFrequency(), tableY - 40, margin, pageWidth);

                yPos -= 110;

                // 5. Terms & Signature
                yPos = drawSectionHeader(contentStream, "TERMS AND CONDITIONS", yPos, margin, pageWidth);
                
                contentStream.setNonStrokingColor(TEXT_GRAY);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                String[] terms = {
                    "• This contract is binding upon the payment of the first premium.",
                    "• Claims must be reported within 48 hours of the incident.",
                    "• The insurer reserves the right to adjust risk coefficients annually.",
                    "• Termination requires a 30-day written notice."
                };
                for (String term : terms) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPos);
                    contentStream.showText(term);
                    contentStream.endText();
                    yPos -= 15;
                }

                yPos -= 40;

                // Signature Area
                float sigY = yPos - 60;
                
                // Admin/Agent Stamp
                if (policy.getAdminStamp() != null) {
                    drawSignature(document, contentStream, policy.getAdminStamp(), margin, sigY, 100, 50);
                }
                
                // Client Signature
                if (policy.getClientSignature() != null) {
                    drawSignature(document, contentStream, policy.getClientSignature(), pageWidth - 150, sigY, 100, 50);
                }

                contentStream.setNonStrokingColor(NAVY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(margin, yPos);
                contentStream.showText("Authorized Signatory");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(pageWidth - 150, yPos);
                contentStream.showText("Policy Holder");
                contentStream.endText();

                contentStream.setStrokingColor(TEXT_GRAY);
                contentStream.moveTo(margin, yPos - 65);
                contentStream.lineTo(margin + 120, yPos - 65);
                contentStream.stroke();

                contentStream.moveTo(pageWidth - 150, yPos - 65);
                contentStream.lineTo(pageWidth - margin, yPos - 65);
                contentStream.stroke();
                
                if (policy.getSignedAt() != null) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                    contentStream.newLineAtOffset(margin, yPos - 75);
                    contentStream.showText("Digitally signed on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(policy.getSignedAt()));
                    contentStream.endText();
                }

                // 6. Footer
                contentStream.setNonStrokingColor(TEXT_GRAY);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                contentStream.newLineAtOffset(margin, 30);
                contentStream.showText("Forsa Insurance Management System - Confidential Document - " + new Date());
                contentStream.endText();
            }

            document.save(outputStream);
        }

        return outputStream;
    }

    private float drawSectionHeader(PDPageContentStream cs, String title, float y, float margin, float width) throws Exception {
        cs.setNonStrokingColor(TEAL);
        cs.addRect(margin, y - 2, width - (margin * 2), 1);
        cs.fill();
        
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        cs.setNonStrokingColor(NAVY);
        cs.newLineAtOffset(margin, y + 5);
        cs.showText(title);
        cs.endText();
        
        return y - 25;
    }

    private void drawRow(PDPageContentStream cs, String label, String value, float y, float margin, float width) throws Exception {
        cs.setNonStrokingColor(NAVY);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        cs.newLineAtOffset(margin + 20, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        cs.newLineAtOffset(width - margin - 150, y);
        cs.showText(value != null ? value : "N/A");
        cs.endText();
    }

    private void drawSignature(PDDocument doc, PDPageContentStream cs, String sigData, float x, float y, float width, float height) throws Exception {
        if (sigData == null || sigData.isEmpty()) return;

        if (sigData.startsWith("data:image")) {
            try {
                String base64Image = sigData.split(",")[1];
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdImage =
                    org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray(doc, imageBytes, "signature");
                cs.drawImage(pdImage, x, y, width, height);
            } catch (Exception e) {
                System.err.println("Error rendering signature image: " + e.getMessage());
            }
        } else {
            cs.beginText();
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC), 14);
            cs.setNonStrokingColor(new Color(0, 51, 153)); // Dark Blue for signature
            cs.newLineAtOffset(x, y + 10);
            cs.showText(sigData);
            cs.endText();
        }
    }
}