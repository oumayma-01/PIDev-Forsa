package org.example.forsapidev.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimTemplate {
    private String policyType;
    private List<FormField> fields;
    private List<DocumentRequirement> requiredDocuments;
    private List<DocumentRequirement> optionalDocuments;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FormField {
        private String name;
        private String label;
        private String type; // e.g., text, number, date, select
        private boolean required;
        private List<String> options; // for select type
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentRequirement {
        private String id;
        private String name;
        private String description;
    }
}
