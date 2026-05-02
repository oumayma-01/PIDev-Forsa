package org.example.forsapidev.Services;

import org.example.forsapidev.Dto.ClaimTemplate;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClaimTemplateService {

    private final Map<String, ClaimTemplate> templates = new HashMap<>();

    public ClaimTemplateService() {
        initializeTemplates();
    }

    private void initializeTemplates() {
        // HEALTH
        ClaimTemplate healthTemplate = new ClaimTemplate(
                "HEALTH",
                Arrays.asList(
                        new ClaimTemplate.FormField("doctorName", "Doctor Name", "text", true, null),
                        new ClaimTemplate.FormField("diagnosis", "Diagnosis", "text", true, null),
                        new ClaimTemplate.FormField("hospitalName", "Hospital/Clinic Name", "text", false, null),
                        new ClaimTemplate.FormField("admissionDate", "Admission Date", "date", false, null),
                        new ClaimTemplate.FormField("dischargeDate", "Discharge Date", "date", false, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("medical_report", "Medical Report", "Detailed report from the attending physician"),
                        new ClaimTemplate.DocumentRequirement("receipts", "Medical Receipts", "Invoices and receipts for expenses")
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("prescriptions", "Prescriptions", "Copies of medical prescriptions")
                )
        );

        // VEHICLE
        ClaimTemplate vehicleTemplate = new ClaimTemplate(
                "VEHICLE",
                Arrays.asList(
                        new ClaimTemplate.FormField("plateNumber", "Vehicle Plate Number", "text", true, null),
                        new ClaimTemplate.FormField("accidentLocation", "Accident Location", "text", true, null),
                        new ClaimTemplate.FormField("driverName", "Driver Name at the time of accident", "text", true, null),
                        new ClaimTemplate.FormField("policeReportNumber", "Police Report Number", "text", false, null),
                        new ClaimTemplate.FormField("otherPartyDetails", "Other Party Details", "textarea", false, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("police_report", "Police Report", "Official police report of the accident"),
                        new ClaimTemplate.DocumentRequirement("vehicle_photos", "Vehicle Photos", "Clear photos showing the damage")
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("driving_license", "Driving License", "Copy of the driver's license"),
                        new ClaimTemplate.DocumentRequirement("repair_estimate", "Repair Estimate", "Quote from an authorized repair shop")
                )
        );

        // LIFE
        ClaimTemplate lifeTemplate = new ClaimTemplate(
                "LIFE",
                Arrays.asList(
                        new ClaimTemplate.FormField("beneficiaryName", "Beneficiary Name", "text", true, null),
                        new ClaimTemplate.FormField("beneficiaryRelation", "Relation to Policy Holder", "text", true, null),
                        new ClaimTemplate.FormField("dateOfDeath", "Date of Death", "date", true, null),
                        new ClaimTemplate.FormField("causeOfDeath", "Cause of Death", "text", true, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("death_certificate", "Death Certificate", "Official death certificate"),
                        new ClaimTemplate.DocumentRequirement("beneficiary_id", "Beneficiary ID", "Valid ID of the claiming beneficiary")
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("medical_records", "Medical Records", "Medical records if applicable")
                )
        );

        // PROPERTY / HOME TEMPLATE
        ClaimTemplate propertyTemplate = new ClaimTemplate("PROPERTY",
                Arrays.asList(
                        new ClaimTemplate.FormField("address", "Property Address", "text", true, null),
                        new ClaimTemplate.FormField("damageType", "Type of Damage (Water, Fire, etc.)", "text", true, null),
                        new ClaimTemplate.FormField("estimatedRepairCost", "Estimated Repair Cost", "number", true, null),
                        new ClaimTemplate.FormField("isHabitable", "Is the property currently habitable?", "text", true, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("photos", "Photos of Damage", null),
                        new ClaimTemplate.DocumentRequirement("repairEstimates", "Repair Estimates", null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("policeReport", "Police Report (if applicable)", null)
                )
        );
        templates.put("PROPERTY", propertyTemplate);

        // CROP TEMPLATE
        ClaimTemplate cropTemplate = new ClaimTemplate("CROP",
                Arrays.asList(
                        new ClaimTemplate.FormField("farmLocation", "Farm / Field Location", "text", true, null),
                        new ClaimTemplate.FormField("cropType", "Type of Crop Affected", "text", true, null),
                        new ClaimTemplate.FormField("acresAffected", "Acres Affected", "number", true, null),
                        new ClaimTemplate.FormField("weatherEvent", "Weather Event Details", "textarea", true, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("fieldPhotos", "Photos of Field Damage", null),
                        new ClaimTemplate.DocumentRequirement("yieldHistory", "Previous Yield History", null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("weatherReport", "Local Weather Report", null)
                )
        );
        templates.put("CROP", cropTemplate);

        // LIVESTOCK TEMPLATE
        ClaimTemplate livestockTemplate = new ClaimTemplate("LIVESTOCK",
                Arrays.asList(
                        new ClaimTemplate.FormField("animalType", "Type of Livestock", "text", true, null),
                        new ClaimTemplate.FormField("numberOfAnimals", "Number of Animals Affected", "number", true, null),
                        new ClaimTemplate.FormField("causeOfDeath", "Cause of Loss/Death", "text", true, null),
                        new ClaimTemplate.FormField("vetName", "Veterinarian Name", "text", false, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("vetReport", "Veterinarian Report", null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("purchaseRecords", "Purchase/Ownership Records", null),
                        new ClaimTemplate.DocumentRequirement("photos", "Photos (if applicable)", null)
                )
        );
        templates.put("LIVESTOCK", livestockTemplate);

        // BUSINESS TEMPLATE
        ClaimTemplate businessTemplate = new ClaimTemplate("BUSINESS",
                Arrays.asList(
                        new ClaimTemplate.FormField("businessName", "Business Name", "text", true, null),
                        new ClaimTemplate.FormField("interruptionDays", "Days of Interruption", "number", true, null),
                        new ClaimTemplate.FormField("lostRevenue", "Estimated Lost Revenue", "number", true, null),
                        new ClaimTemplate.FormField("incidentDetails", "Details of Incident", "textarea", true, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("financialRecords", "Financial Statements / Ledgers", null),
                        new ClaimTemplate.DocumentRequirement("incidentReport", "Incident Report", null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("taxReturns", "Recent Tax Returns", null)
                )
        );
        templates.put("BUSINESS", businessTemplate);

        // GENERAL TEMPLATE (Fallback)
        ClaimTemplate generalTemplate = new ClaimTemplate("GENERAL",
                Arrays.asList(
                        new ClaimTemplate.FormField("incidentLocation", "Location of Incident", "text", true, null),
                        new ClaimTemplate.FormField("details", "Specific Details", "textarea", true, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("evidence", "Evidence / Photos", null)
                ),
                Arrays.asList()
        );
        templates.put("GENERAL", generalTemplate);

        // HOME
        ClaimTemplate homeTemplate = new ClaimTemplate(
                "HOME",
                Arrays.asList(
                        new ClaimTemplate.FormField("propertyAddress", "Property Address", "text", true, null),
                        new ClaimTemplate.FormField("damageType", "Type of Damage", "select", true, Arrays.asList("Fire", "Water", "Theft", "Natural Disaster", "Other")),
                        new ClaimTemplate.FormField("estimatedLoss", "Estimated Loss Value", "number", true, null)
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("damage_photos", "Photos of Damage", "Clear photos of the damaged property"),
                        new ClaimTemplate.DocumentRequirement("ownership_proof", "Proof of Ownership", "Deed or recent utility bill")
                ),
                Arrays.asList(
                        new ClaimTemplate.DocumentRequirement("repair_quotes", "Repair Quotes", "Estimates from contractors"),
                        new ClaimTemplate.DocumentRequirement("police_report_theft", "Police Report (if theft)", "Police report if claim is for theft")
                )
        );

        templates.put("HEALTH", healthTemplate);
        templates.put("VEHICLE", vehicleTemplate);
        templates.put("LIFE", lifeTemplate);
        templates.put("HOME", homeTemplate);
    }

    public ClaimTemplate getTemplateForPolicyType(String policyType) {
        return templates.get(policyType != null ? policyType.toUpperCase() : "VEHICLE");
    }

    public List<String> getAvailablePolicyTypes() {
        return Arrays.asList("HEALTH", "VEHICLE", "LIFE", "HOME");
    }
}
