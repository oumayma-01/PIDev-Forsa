package org.example.forsapidev.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Schema(name = "CreditRequestMultipart", description = "Modèle multipart pour la création de crédit (docs only)")
public class CreditRequestMultipart {

    @Schema(description = "Montant demandé", required = true, type = "number", example = "700000")
    private BigDecimal amountRequested;

    @Schema(description = "Durée en mois", required = true, type = "integer", example = "20")
    private Integer durationMonths;

    @Schema(description = "Type de calcul", example = "AMORTISSEMENT_CONSTANT")
    private String typeCalcul;

    @Schema(description = "Rapport médical (PDF ou image)", type = "string", format = "binary")
    private MultipartFile healthReport;

    @Schema(description = "Nom complet du garant", example = "Mohamed Ben Ali")
    private String guarantorName;

    @Schema(description = "Numéro CIN du garant", example = "12345678")
    private String guarantorCin;

    @Schema(description = "Compte bancaire du garant (RIB)", example = "07 123 0123456789 01")
    private String guarantorBankAccount;

    @Schema(description = "Photo du garant avec sa carte CIN", type = "string", format = "binary")
    private MultipartFile guarantorPhoto;

    // getters & setters
    public BigDecimal getAmountRequested() { return amountRequested; }
    public void setAmountRequested(BigDecimal amountRequested) { this.amountRequested = amountRequested; }

    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }

    public String getTypeCalcul() { return typeCalcul; }
    public void setTypeCalcul(String typeCalcul) { this.typeCalcul = typeCalcul; }

    public MultipartFile getHealthReport() { return healthReport; }
    public void setHealthReport(MultipartFile healthReport) { this.healthReport = healthReport; }

    public String getGuarantorName() { return guarantorName; }
    public void setGuarantorName(String guarantorName) { this.guarantorName = guarantorName; }

    public String getGuarantorCin() { return guarantorCin; }
    public void setGuarantorCin(String guarantorCin) { this.guarantorCin = guarantorCin; }

    public String getGuarantorBankAccount() { return guarantorBankAccount; }
    public void setGuarantorBankAccount(String guarantorBankAccount) { this.guarantorBankAccount = guarantorBankAccount; }

    public MultipartFile getGuarantorPhoto() { return guarantorPhoto; }
    public void setGuarantorPhoto(MultipartFile guarantorPhoto) { this.guarantorPhoto = guarantorPhoto; }
}

