package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.DTO.ComparisonResultDTO;
import org.example.forsapidev.DTO.InsuranceProductComparisonDTO;
import org.example.forsapidev.Repositories.InsuranceProductRepository;
import org.example.forsapidev.Services.Interfaces.IInsuranceProductComparisonService;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InsuranceProductComparisonService implements IInsuranceProductComparisonService {

    private final InsuranceProductRepository productRepository;

    public InsuranceProductComparisonService(InsuranceProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ComparisonResultDTO compareProducts(List<Long> productIds) {
        if (productIds == null || productIds.size() < 2 || productIds.size() > 3) {
            throw new IllegalArgumentException("Please select 2 or 3 products to compare");
        }

        ComparisonResultDTO result = new ComparisonResultDTO();
        List<InsuranceProductComparisonDTO> comparisonList = new ArrayList<>();

        // Fetch and convert products
        for (Long productId : productIds) {
            InsuranceProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            InsuranceProductComparisonDTO dto = convertToComparisonDTO(product);
            comparisonList.add(dto);
        }

        // Calculate value scores
        calculateValueScores(comparisonList);

        // Determine best options
        result.setProducts(comparisonList);
        result.setBestValueProductId(findBestValue(comparisonList));
        result.setLowestPremiumProductId(findLowestPremium(comparisonList));
        result.setHighestCoverageProductId(findHighestCoverage(comparisonList));
        result.setComparisonSummary(generateSummary(comparisonList, result));

        System.out.println("✅ Comparison completed for " + productIds.size() + " products");
        return result;
    }

    private InsuranceProductComparisonDTO convertToComparisonDTO(InsuranceProduct product) {
        InsuranceProductComparisonDTO dto = new InsuranceProductComparisonDTO();

        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setPolicyType(product.getPolicyType());
        dto.setPremiumAmount(product.getPremiumAmount());
        dto.setCoverageLimit(product.getCoverageLimit());
        dto.setDurationMonths(product.getDurationMonths());
        dto.setDescription(product.getDescription());
        dto.setIsActive(product.getIsActive());

        // Calculate cost per month
        if (product.getDurationMonths() != null && product.getDurationMonths() > 0) {
            BigDecimal costPerMonth = product.getPremiumAmount()
                    .divide(new BigDecimal(product.getDurationMonths()), 2, RoundingMode.HALF_UP);
            dto.setCostPerMonth(costPerMonth);
        }

        // Calculate coverage per dollar (how much coverage you get per $1 premium)
        if (product.getPremiumAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal coveragePerDollar = product.getCoverageLimit()
                    .divide(product.getPremiumAmount(), 2, RoundingMode.HALF_UP);
            dto.setCoveragePerDollar(coveragePerDollar);
        }

        return dto;
    }

    private void calculateValueScores(List<InsuranceProductComparisonDTO> products) {
        // Calculate value score: (Coverage / Premium) * 100
        for (InsuranceProductComparisonDTO product : products) {
            if (product.getPremiumAmount().compareTo(BigDecimal.ZERO) > 0) {
                double score = product.getCoverageLimit()
                        .divide(product.getPremiumAmount(), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                product.setValueScore(score);
            } else {
                product.setValueScore(0.0);
            }
        }

        // Find max score for rating
        double maxScore = products.stream()
                .mapToDouble(p -> p.getValueScore())
                .max()
                .orElse(0.0);

        // Assign ratings
        for (InsuranceProductComparisonDTO product : products) {
            double score = product.getValueScore();
            if (score == maxScore) {
                product.setValueRating("Best Value");
            } else if (score >= maxScore * 0.8) {
                product.setValueRating("Good Value");
            } else {
                product.setValueRating("Standard");
            }
        }
    }

    private Long findBestValue(List<InsuranceProductComparisonDTO> products) {
        return products.stream()
                .max(Comparator.comparing(InsuranceProductComparisonDTO::getValueScore))
                .map(InsuranceProductComparisonDTO::getId)
                .orElse(null);
    }

    private Long findLowestPremium(List<InsuranceProductComparisonDTO> products) {
        return products.stream()
                .min(Comparator.comparing(InsuranceProductComparisonDTO::getPremiumAmount))
                .map(InsuranceProductComparisonDTO::getId)
                .orElse(null);
    }

    private Long findHighestCoverage(List<InsuranceProductComparisonDTO> products) {
        return products.stream()
                .max(Comparator.comparing(InsuranceProductComparisonDTO::getCoverageLimit))
                .map(InsuranceProductComparisonDTO::getId)
                .orElse(null);
    }

    private String generateSummary(List<InsuranceProductComparisonDTO> products, ComparisonResultDTO result) {
        InsuranceProductComparisonDTO bestValue = products.stream()
                .filter(p -> p.getId().equals(result.getBestValueProductId()))
                .findFirst()
                .orElse(null);

        if (bestValue != null) {
            return String.format(
                    "%s offers the best value with $%.2f coverage per dollar spent. " +
                            "It provides $%s coverage for $%s premium over %d months.",
                    bestValue.getProductName(),
                    bestValue.getCoveragePerDollar(),
                    bestValue.getCoverageLimit().toString(),
                    bestValue.getPremiumAmount().toString(),
                    bestValue.getDurationMonths()
            );
        }

        return "Compare the products above to find the best option for your needs.";
    }
}