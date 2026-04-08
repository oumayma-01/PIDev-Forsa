package org.example.forsapidev.Services.Interfaces;

import java.io.ByteArrayOutputStream;

public interface IAmortizationPdfService {
    ByteArrayOutputStream generateAmortizationTablePdf(Long policyId) throws Exception;
}
