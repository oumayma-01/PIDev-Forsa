package org.example.forsapidev.Services.implementation;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Services.Interfaces.IRoleService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
class RoleService implements IRoleService {

    @Autowired

    RoleRepository roleRepository;


    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role findbyId(Integer id) {
        return roleRepository.findById(id).get();
    }
    @Override
    public Role findbyName(ERole name) {
        return roleRepository.findByName(name).get();
    }


    @Override
    public ResponseEntity<?> delete(Integer id) {
        Optional<Role> role = roleRepository.findById(id);
        Role r = role.get();
        roleRepository.delete(r);
        return ResponseEntity.ok("The role has been successfully deleted") ;
    }

    @Service
    public static class OpenAIService {

        @Value("${openai.api.key}")
        private String apiKey;

        private static final String OPENAI_URL =
                "https://api.openai.com/v1/chat/completions";

        // ================================================
        // 🤖 MÉTHODE GÉNÉRIQUE POUR APPELER OPENAI
        // ================================================
        public String callOpenAI(String systemMessage,
                                 String userMessage) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ObjectMapper mapper = new ObjectMapper();

                // 1. Construire le body de la requête
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "gpt-3.5-turbo");
                requestBody.put("max_tokens", 500);
                requestBody.put("temperature", 0.7);

                List<Map<String, String>> messages =
                        new ArrayList<>();
                messages.add(Map.of(
                        "role", "system",
                        "content", systemMessage));
                messages.add(Map.of(
                        "role", "user",
                        "content", userMessage));

                requestBody.put("messages", messages);

                // 2. Configurer les headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(
                        MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> entity =
                        new HttpEntity<>(requestBody, headers);

                // 3. Envoyer la requête
                ResponseEntity<String> response =
                        restTemplate.postForEntity(
                                OPENAI_URL, entity, String.class);

                // 4. Parser la réponse
                JsonNode root = mapper.readTree(
                        response.getBody());
                return root.path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

            } catch (Exception e) {
                return "Erreur lors de l'appel à OpenAI: "
                        + e.getMessage();
            }
        }

        // ================================================
        // ✅ CLASSIFIER PAR CATÉGORIE
        // ================================================
        public String classifyCategory(String subject,
                                       String description) {
            String systemMsg = """
                Tu es un expert en classification 
                de réclamations bancaires.
                Réponds UNIQUEMENT avec une de ces 
                catégories (rien d'autre) :
                TECHNIQUE, FINANCE, SUPPORT, FRAUDE, 
                COMPTE, CREDIT, AUTRE
                """;

            String userMsg = String.format(
                    "Sujet: %s\nDescription: %s\nCatégorie:",
                    subject, description
            );

            return callOpenAI(systemMsg, userMsg);
        }

        // ================================================
        // ✅ ANALYSER L'URGENCE
        // ================================================
        public String analyzeUrgency(String subject,
                                     String description) {
            String systemMsg = """
                Tu es un expert en analyse d'urgence 
                de réclamations bancaires.
                Réponds UNIQUEMENT avec un de ces 
                niveaux (rien d'autre) :
                CRITIQUE, URGENT, NORMAL, FAIBLE
                """;

            String userMsg = String.format(
                    "Sujet: %s\nDescription: %s\nUrgence:",
                    subject, description
            );

            return callOpenAI(systemMsg, userMsg);
        }

        // ================================================
        // ✅ GÉNÉRER UNE RÉPONSE AUTOMATIQUE
        // ================================================
        public String generateResponse(String subject,
                                       String description,
                                       String category) {
            String systemMsg = """
                Tu es un agent support professionnel 
                dans une banque tunisienne (ForsaPidev).
                Génère des réponses professionnelles, 
                empathiques et en français (3-5 phrases).
                """;

            String userMsg = String.format("""
                Génère une réponse pour cette réclamation :
                
                Catégorie : %s
                Sujet : %s
                Description : %s
                
                La réponse doit :
                ✓ Remercier le client
                ✓ Montrer de l'empathie
                ✓ Proposer une solution
                ✓ Formule de politesse
                """, category, subject, description);

            return callOpenAI(systemMsg, userMsg);
        }

        // ================================================
        // ✅ GÉNÉRER UN RAPPORT
        // ================================================
        public String generateReport(
                Map<String, Object> stats) {
            String systemMsg = """
                Tu es un analyste de données senior 
                dans une banque tunisienne.
                Génère des rapports professionnels 
                en français, clairs et structurés.
                """;

            String userMsg = String.format("""
                Génère un rapport d'analyse basé 
                sur ces données :
                
                %s
                
                Inclus : résumé, points forts, 
                points à améliorer, recommandations
                """, stats.toString());

            return callOpenAI(systemMsg, userMsg);
        }
    }
}
