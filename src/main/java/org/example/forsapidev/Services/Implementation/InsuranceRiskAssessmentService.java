package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Config.ActuarialConstants;
import org.example.forsapidev.DTO.InsuranceRiskAssessmentDTO;
import org.example.forsapidev.Services.Interfaces.IInsuranceRiskAssessmentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InsuranceRiskAssessmentService implements IInsuranceRiskAssessmentService {

    @Override
    public InsuranceRiskAssessmentDTO calculateRiskScore(InsuranceRiskAssessmentDTO riskProfile) {
        double totalScore = 0.0;

        // 1. Age Factor (0-1 normalized, higher age = higher risk)
        double ageFactor = calculateAgeFactor(riskProfile.getAge());
        totalScore += ageFactor * ActuarialConstants.AGE_WEIGHT;

        // 2. Income Factor (0-1 normalized, higher income = lower risk)
        double incomeFactor = calculateIncomeFactor(riskProfile.getMonthlyIncome());
        totalScore += incomeFactor * ActuarialConstants.INCOME_WEIGHT;

        // 3. Health Factor (0-1 normalized)
        double healthFactor = calculateHealthFactor(riskProfile);
        totalScore += healthFactor * ActuarialConstants.HEALTH_WEIGHT;

        // 4. Occupation Factor (0-1 normalized)
        double occupationFactor = calculateOccupationFactor(riskProfile.getOccupationType());
        totalScore += occupationFactor * ActuarialConstants.OCCUPATION_WEIGHT;

        // 5. Location Factor (0-1 normalized)
        double locationFactor = calculateLocationFactor(riskProfile.getLocationRiskLevel());
        totalScore += locationFactor * ActuarialConstants.LOCATION_WEIGHT;

        // 6. Lifestyle Factor (0-1 normalized)
        double lifestyleFactor = calculateLifestyleFactor(riskProfile);
        totalScore += lifestyleFactor * ActuarialConstants.LIFESTYLE_WEIGHT;

        // Set calculated values
        riskProfile.setRiskScore(totalScore);
        riskProfile.setRiskCategory(determineRiskCategory(totalScore));
        riskProfile.setRiskCoefficient(determineRiskCoefficient(totalScore));

        System.out.println("✅ Risk assessment completed: Score = " + totalScore +
                ", Category = " + riskProfile.getRiskCategory());

        return riskProfile;
    }

    private double calculateAgeFactor(Integer age) {
        if (age == null) return 0.5;

        // Normalize age: 18-30 = low risk, 30-50 = medium, 50+ = high
        if (age < 30) return 0.3;
        else if (age < 45) return 0.5;
        else if (age < 60) return 0.7;
        else return 0.9;
    }

    private double calculateIncomeFactor(BigDecimal monthlyIncome) {
        if (monthlyIncome == null) return 0.5;

        double income = monthlyIncome.doubleValue();

        // Lower income = higher risk (inability to pay)
        if (income < 500) return 0.8;
        else if (income < 1000) return 0.6;
        else if (income < 2000) return 0.4;
        else return 0.2;
    }

    private double calculateHealthFactor(InsuranceRiskAssessmentDTO profile) {
        double healthScore = 0.5; // default

        // Health status
        String healthStatus = profile.getHealthStatus();
        if ("EXCELLENT".equalsIgnoreCase(healthStatus)) {
            healthScore = 0.2;
        } else if ("GOOD".equalsIgnoreCase(healthStatus)) {
            healthScore = 0.4;
        } else if ("FAIR".equalsIgnoreCase(healthStatus)) {
            healthScore = 0.6;
        } else if ("POOR".equalsIgnoreCase(healthStatus)) {
            healthScore = 0.9;
        }

        // Chronic illness adds risk
        if (Boolean.TRUE.equals(profile.getHasChronicIllness())) {
            healthScore += 0.2;
        }

        return Math.min(healthScore, 1.0);
    }

    private double calculateOccupationFactor(String occupationType) {
        if (occupationType == null) return 0.5;

        String occupation = occupationType.toUpperCase();

        // Low risk occupations
        if (occupation.contains("OFFICE") || occupation.contains("TEACHER") ||
                occupation.contains("ADMIN")) {
            return 0.3;
        }

        // Medium risk occupations
        if (occupation.contains("SHOP") || occupation.contains("FARMER") ||
                occupation.contains("VENDOR")) {
            return 0.5;
        }

        // High risk occupations
        if (occupation.contains("CONSTRUCTION") || occupation.contains("DRIVER") ||
                occupation.contains("FACTORY")) {
            return 0.8;
        }

        return 0.5; // default medium
    }

    private double calculateLocationFactor(String locationRiskLevel) {
        if (locationRiskLevel == null) return 0.5;

        switch (locationRiskLevel.toUpperCase()) {
            case "LOW":
                return 0.3;
            case "MEDIUM":
                return 0.5;
            case "HIGH":
                return 0.8;
            default:
                return 0.5;
        }
    }

    private double calculateLifestyleFactor(InsuranceRiskAssessmentDTO profile) {
        double lifestyleScore = 0.3; // default low risk

        // Smoking increases risk
        if (Boolean.TRUE.equals(profile.getIsSmoker())) {
            lifestyleScore += 0.3;
        }

        // Dependents slightly increase financial risk
        Integer dependents = profile.getDependents();
        if (dependents != null && dependents > 3) {
            lifestyleScore += 0.2;
        }

        return Math.min(lifestyleScore, 1.0);
    }

    private String determineRiskCategory(double riskScore) {
        if (riskScore < ActuarialConstants.LOW_RISK_THRESHOLD) {
            return "LOW_RISK";
        } else if (riskScore < ActuarialConstants.MEDIUM_RISK_THRESHOLD) {
            return "MEDIUM_RISK";
        } else {
            return "HIGH_RISK";
        }
    }

    private double determineRiskCoefficient(double riskScore) {
        if (riskScore < ActuarialConstants.LOW_RISK_THRESHOLD) {
            return ActuarialConstants.LOW_RISK_COEFFICIENT;
        } else if (riskScore < ActuarialConstants.MEDIUM_RISK_THRESHOLD) {
            return ActuarialConstants.MEDIUM_RISK_COEFFICIENT;
        } else {
            return ActuarialConstants.HIGH_RISK_COEFFICIENT;
        }
    }
}