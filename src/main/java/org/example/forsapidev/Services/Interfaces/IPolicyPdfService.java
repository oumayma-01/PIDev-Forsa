package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import java.io.ByteArrayOutputStream;

public interface IPolicyPdfService {
    ByteArrayOutputStream generatePolicyPdf(Long policyId) throws Exception;
    ByteArrayOutputStream generatePolicyPdf(InsurancePolicy policy) throws Exception;
}