package org.example.forsapidev.Services.Implementation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.example.forsapidev.Config.PdfConfig;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Services.Interfaces.IPolicyPdfService;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

@Service
public class PolicyPdfService implements IPolicyPdfService {

    private final InsurancePolicyRepository policyRepository;
    private final PdfConfig pdfConfig;

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
                float yPosition = 750;
                float margin = 50;
                float pageWidth = page.getMediaBox().getWidth();

                // Add header with logo and company info
                yPosition = addHeader(contentStream, document, yPosition, margin);

                // Add horizontal line
                yPosition -= 20;
                drawLine(contentStream, margin, yPosition, pageWidth - margin, yPosition);
                yPosition -= 30;

                // Add title
                yPosition = addTitle(contentStream, "INSURANCE POLICY CERTIFICATE", yPosition, pageWidth);
                yPosition -= 30;

                // Add policy details section
                yPosition = addSectionTitle(contentStream, "Policy Information", yPosition, margin);
                yPosition -= 20;
                yPosition = addPolicyDetails(contentStream, policy, yPosition, margin);
                yPosition -= 30;

                // Add coverage details section
                yPosition = addSectionTitle(contentStream, "Coverage Details", yPosition, margin);
                yPosition -= 20;
                yPosition = addCoverageDetails(contentStream, policy, yPosition, margin);
                yPosition -= 30;

                // Add beneficiary section (if user exists)
                if (policy.getUser() != null) {
                    yPosition = addSectionTitle(contentStream, "Policy Holder Information", yPosition, margin);
                    yPosition -= 20;
                    yPosition = addBeneficiaryDetails(contentStream, policy, yPosition, margin);
                    yPosition -= 30;
                }

                // Add terms and conditions
                yPosition = addSectionTitle(contentStream, "Terms & Conditions", yPosition, margin);
                yPosition -= 20;
                yPosition = addTermsAndConditions(contentStream, yPosition, margin);

                // Add footer
                addFooter(contentStream, page, margin, pageWidth);
            }

            document.save(outputStream);
            System.out.println("✅ PDF generated successfully for policy: " + policy.getPolicyNumber());
        }

        return outputStream;
    }

    private float addHeader(PDPageContentStream contentStream, PDDocument document,
                            float yPosition, float margin) throws IOException {
        // Try to add logo
        try {
            File logoFile = new File(pdfConfig.getLogoPath());
            if (logoFile.exists()) {
                PDImageXObject logo = PDImageXObject.createFromFile(pdfConfig.getLogoPath(), document);
                float logoHeight = 40;
                float logoWidth = logo.getWidth() * (logoHeight / logo.getHeight());
                contentStream.drawImage(logo, margin, yPosition - logoHeight, logoWidth, logoHeight);
                yPosition -= (logoHeight + 10);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Logo not found, using text header");
        }

        // Company name and info
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(pdfConfig.getCompanyName());
        contentStream.endText();
        yPosition -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(pdfConfig.getCompanyAddress());
        contentStream.endText();
        yPosition -= 15;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Phone: " + pdfConfig.getCompanyPhone() + " | Email: " + pdfConfig.getCompanyEmail());
        contentStream.endText();
        yPosition -= 10;

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
        return yPosition - 20;
    }

    private float addSectionTitle(PDPageContentStream contentStream, String title,
                                  float yPosition, float margin) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        return yPosition - 15;
    }

    private float addPolicyDetails(PDPageContentStream contentStream, InsurancePolicy policy,
                                   float yPosition, float margin) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        yPosition = addKeyValue(contentStream, "Policy Number:", policy.getPolicyNumber(), yPosition, margin);
        yPosition = addKeyValue(contentStream, "Policy Type:", policy.getPolicyType(), yPosition, margin);
        yPosition = addKeyValue(contentStream, "Status:", policy.getStatus().toString(), yPosition, margin);
        yPosition = addKeyValue(contentStream, "Issue Date:", dateFormat.format(policy.getStartDate()), yPosition, margin);
        yPosition = addKeyValue(contentStream, "Expiry Date:", dateFormat.format(policy.getEndDate()), yPosition, margin);
        yPosition = addKeyValue(contentStream, "Next Premium Due:",
                dateFormat.format(policy.getNextPremiumDueDate()), yPosition, margin);

        return yPosition;
    }

    private float addCoverageDetails(PDPageContentStream contentStream, InsurancePolicy policy,
                                     float yPosition, float margin) throws IOException {
        yPosition = addKeyValue(contentStream, "Premium Amount:",
                "$" + policy.getPremiumAmount().toString(), yPosition, margin);
        yPosition = addKeyValue(contentStream, "Coverage Limit:",
                "$" + policy.getCoverageLimit().toString(), yPosition, margin);

        if (policy.getInsuranceProduct() != null) {
            yPosition = addKeyValue(contentStream, "Product Name:",
                    policy.getInsuranceProduct().getProductName(), yPosition, margin);
        }

        return yPosition;
    }

    private float addBeneficiaryDetails(PDPageContentStream contentStream, InsurancePolicy policy,
                                        float yPosition, float margin) throws IOException {
        if (policy.getUser() != null) {
            yPosition = addKeyValue(contentStream, "Name:", policy.getUser().getUsername(), yPosition, margin);
            yPosition = addKeyValue(contentStream, "Email:", policy.getUser().getEmail(), yPosition, margin);
        }
        return yPosition;
    }

    private float addTermsAndConditions(PDPageContentStream contentStream, float yPosition,
                                        float margin) throws IOException {
        String[] terms = {
                "1. This policy is valid only when premiums are paid on time.",
                "2. Policy holder must notify the company of any changes within 30 days.",
                "3. Claims must be filed within 60 days of the incident.",
                "4. This policy is non-transferable without written consent.",
                "5. Coverage begins on the start date specified above.",
                "6. Company reserves the right to modify terms with 30 days notice."
        };

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        for (String term : terms) {
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(term);
            contentStream.endText();
            yPosition -= 12;
        }

        return yPosition;
    }

    private float addKeyValue(PDPageContentStream contentStream, String key, String value,
                              float yPosition, float margin) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(key);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(margin + 150, yPosition);
        contentStream.showText(value != null ? value : "N/A");
        contentStream.endText();

        return yPosition - 15;
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
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.newLineAtOffset(margin, footerY);
        contentStream.showText("This is a computer-generated document. No signature required.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
        contentStream.newLineAtOffset(margin, footerY - 12);
        contentStream.showText("Generated on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        String pageNum = "Page 1 of 1";
        float pageNumWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                .getStringWidth(pageNum) / 1000 * 8;
        contentStream.newLineAtOffset(pageWidth - margin - pageNumWidth, footerY);
        contentStream.showText(pageNum);
        contentStream.endText();
    }
}