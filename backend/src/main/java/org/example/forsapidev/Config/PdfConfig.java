package org.example.forsapidev.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PdfConfig {

    @Value("${app.pdf.logo.path}")
    private String logoPath;

    @Value("${app.pdf.company.name}")
    private String companyName;

    @Value("${app.pdf.company.address}")
    private String companyAddress;

    @Value("${app.pdf.company.phone}")
    private String companyPhone;

    @Value("${app.pdf.company.email}")
    private String companyEmail;

    // Getters
    public String getLogoPath() { return logoPath; }
    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public String getCompanyPhone() { return companyPhone; }
    public String getCompanyEmail() { return companyEmail; }
}
