package org.example.forsapidev.Config;

import java.math.BigDecimal;

public class ActuarialConstants {

    // PREMIUM CALCULATION RATES
    public static final double MANAGEMENT_FEE_RATE = 0.15;  // g = 15%
    public static final double ACQUISITION_COST_RATE = 0.10;  // α = 10%
    public static final double PROFIT_MARGIN_RATE = 0.08;  // 8% profit margin

    // INTEREST RATES (for present value calculations)
    public static final double ANNUAL_TECHNICAL_RATE = 0.05;  // i = 5% per year
    public static final double MONTHLY_RATE = ANNUAL_TECHNICAL_RATE / 12;

    // CLAIM FREQUENCY BY INSURANCE TYPE (λ - expected claims per year)
    public static final double HEALTH_CLAIM_FREQUENCY = 0.25;  // 25% chance per year
    public static final double LIFE_CLAIM_FREQUENCY = 0.01;    // 1% mortality rate
    public static final double PROPERTY_CLAIM_FREQUENCY = 0.05;  // 5% chance
    public static final double ACCIDENT_CLAIM_FREQUENCY = 0.08;  // 8% chance
    public static final double CROP_CLAIM_FREQUENCY = 0.20;    // 20% chance
    public static final double LIVESTOCK_CLAIM_FREQUENCY = 0.15; // 15% chance
    public static final double BUSINESS_CLAIM_FREQUENCY = 0.10;  // 10% chance

    // AVERAGE CLAIM SEVERITY (% of coverage amount)
    public static final double HEALTH_SEVERITY = 0.30;    // 30% of coverage
    public static final double LIFE_SEVERITY = 1.00;      // 100% (death benefit)
    public static final double PROPERTY_SEVERITY = 0.40;  // 40% of property value
    public static final double ACCIDENT_SEVERITY = 0.50;  // 50% of coverage
    public static final double CROP_SEVERITY = 0.60;      // 60% of crop value
    public static final double LIVESTOCK_SEVERITY = 0.70; // 70% of animal value
    public static final double BUSINESS_SEVERITY = 0.35;  // 35% of coverage

    // RISK SCORING WEIGHTS (total = 1.0)
    public static final double AGE_WEIGHT = 0.25;
    public static final double INCOME_WEIGHT = 0.20;
    public static final double HEALTH_WEIGHT = 0.20;
    public static final double OCCUPATION_WEIGHT = 0.15;
    public static final double LOCATION_WEIGHT = 0.10;
    public static final double LIFESTYLE_WEIGHT = 0.10;

    // RISK CATEGORIES THRESHOLDS
    public static final double LOW_RISK_THRESHOLD = 0.30;
    public static final double MEDIUM_RISK_THRESHOLD = 0.60;
    // Above 0.60 = HIGH_RISK

    // RISK COEFFICIENT MULTIPLIERS
    public static final double LOW_RISK_COEFFICIENT = 0.80;     // 20% discount
    public static final double MEDIUM_RISK_COEFFICIENT = 1.00;  // no change
    public static final double HIGH_RISK_COEFFICIENT = 1.50;    // 50% surcharge

    // PAYMENT FREQUENCY FACTORS
    public static final int MONTHLY_PERIODS = 12;
    public static final int QUARTERLY_PERIODS = 4;
    public static final int SEMI_ANNUAL_PERIODS = 2;
    public static final int ANNUAL_PERIODS = 1;

    // MINIMUM AND MAXIMUM VALUES
    public static final BigDecimal MIN_COVERAGE_AMOUNT = new BigDecimal("1000");
    public static final BigDecimal MAX_COVERAGE_AMOUNT = new BigDecimal("100000");
    public static final int MIN_DURATION_MONTHS = 6;
    public static final int MAX_DURATION_MONTHS = 60;
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 65;

    // OCCUPATION RISK FACTORS
    public static final double LOW_RISK_OCCUPATION = 0.80;   // Office worker, teacher
    public static final double MEDIUM_RISK_OCCUPATION = 1.00; // Shopkeeper, farmer
    public static final double HIGH_RISK_OCCUPATION = 1.40;   // Construction, driver

    // LOCATION RISK FACTORS
    public static final double LOW_RISK_LOCATION = 0.90;   // Urban, secure area
    public static final double MEDIUM_RISK_LOCATION = 1.00; // Suburban
    public static final double HIGH_RISK_LOCATION = 1.30;   // Rural, flood-prone

    // HEALTH STATUS FACTORS
    public static final double EXCELLENT_HEALTH = 0.85;
    public static final double GOOD_HEALTH = 1.00;
    public static final double FAIR_HEALTH = 1.20;
    public static final double POOR_HEALTH = 1.50;

    // LIFESTYLE FACTORS
    public static final double SMOKER_FACTOR = 1.30;
    public static final double NON_SMOKER_FACTOR = 1.00;
    public static final double CHRONIC_ILLNESS_FACTOR = 1.40;
}