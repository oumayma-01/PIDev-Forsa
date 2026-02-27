package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.ComparisonResultDTO;
import java.io.ByteArrayOutputStream;

public interface IComparisonPdfService {
    ByteArrayOutputStream generateComparisonPdf(ComparisonResultDTO comparison) throws Exception;
}