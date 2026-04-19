package org.example.forsapidev.security.access;

import lombok.Getter;
import org.example.forsapidev.entities.UserManagement.ERole;

import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Zones fonctionnelles de l'API (préfixes {@code servletPath}), alignées sur les
 * {@code @RequestMapping} des contrôleurs. Les drapeaux {@code defaultAgent}/{@code defaultClient}
 * reflètent approximativement les {@code @PreAuthorize} existants au moment de l'introduction
 * du module ; l'administrateur peut les ajuster via l'API.
 */
@Getter
public enum PlatformAccessResource {
  ACCOUNTS("/api/accounts/**", "Comptes", "Gestion des comptes et soldes (réservé administration).", false, false),
  ACTUARIAL("/api/actuarial/**", "Actuariat", "Calculs et indicateurs actuariels.", true, true),
  AGENTS("/api/agents/**", "Agents", "Gestion des agents et tâches terrain.", true, false),
  CASHBACK("/api/cashback/**", "Cashback", "Programmes et opérations cashback.", true, true),
  CHATBOT("/api/chatbot/**", "Assistant", "Chatbot et assistance automatisée.", true, true),
  CLAIMS_DASHBOARD("/api/claims-dashboard/**", "Tableau sinistres", "Vue consolidée des sinistres.", true, false),
  COMPLAINTS("/api/complaints/**", "Réclamations", "Réclamations et suivi litiges.", true, true),
  CREDITS("/api/credits/**", "Crédits", "Demandes de crédit, validation et suivi.", true, true),
  FEEDBACKS("/api/feedbacks/**", "Retours", "Avis et retours utilisateurs.", true, true),
  FRAUD_ALERTS("/api/fraud-alerts/**", "Alertes fraude", "Surveillance et alertes fraude.", true, false),
  INSURANCE_CLAIM("/api/insurance-claim/**", "Sinistres", "Déclaration et gestion des sinistres.", true, true),
  INSURANCE_POLICY("/api/insurance-policy/**", "Polices", "Contrats et polices d'assurance.", true, true),
  INSURANCE_PRODUCT("/api/insurance-product/**", "Produits", "Catalogue et produits d'assurance.", true, true),
  INTEGRATED_POLICY("/integrated-policy/**", "Polices intégrées", "Flux intégré des polices.", true, false),
  PARTNER_ANALYTICS("/api/partner-analytics/**", "Analytics partenaires", "Statistiques partenaires.", true, false),
  PARTNERS("/api/partners/**", "Partenaires", "Gestion des partenaires.", true, true),
  PARTNER_REVIEWS("/api/partner-reviews/**", "Avis partenaires", "Évaluations des partenaires.", true, true),
  PARTNER_TRANSACTIONS("/api/partner-transactions/**", "Transactions partenaires", "Transactions liées aux partenaires.", true, true),
  POLICY_PDF("/api/policy-pdf/**", "PDF polices", "Génération et téléchargement de polices PDF.", true, true),
  PREMIUM_PAYMENT("/api/premium-payment/**", "Paiement primes", "Paiement et suivi des primes.", true, true),
  PREMIUM_REMINDER("/api/premium-reminder/**", "Rappels primes", "Rappels et relances de paiement (administration).", false, false),
  PRODUCT_COMPARISON("/api/product-comparison/**", "Comparaison produits", "Comparaison d'offres d'assurance.", true, true),
  PROFILE("/api/profile/**", "Profil", "Profil utilisateur connecté (tous rôles).", true, true),
  QR_CODE("/api/qr-code/**", "QR codes", "Génération ou lecture de QR codes.", true, true),
  RECOMMENDATIONS("/api/recommendations/**", "Recommandations", "Recommandations personnalisées.", true, true),
  REPAYMENTS("/api/repayments/**", "Remboursements", "Échéanciers et remboursements.", true, true),
  RESPONSES("/api/responses/**", "Réponses", "Réponses agents sur dossiers clients.", true, false),
  ROLE_MANAGEMENT("/api/role/**", "Rôles & accès", "Administration des rôles et des droits d'accès.", false, false),
  SCORING("/api/scoring/**", "Scoring", "Scoring crédit et risques.", true, true),
  ADMIN_TMM("/api/admin/tmm/**", "Taux TMM", "Paramétrage des taux administratifs.", false, false),
  USER_ADMIN("/api/user/**", "Utilisateurs", "Administration des comptes utilisateurs.", false, false),
  DASHBOARD_USERS("/api/dashboard/users/**", "Vue utilisateurs", "Tableau de bord utilisateurs (admin / agent).", true, false);

  private final String pathPattern;
  private final String title;
  private final String description;
  private final boolean defaultAgent;
  private final boolean defaultClient;

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  private static final List<PlatformAccessResource> LONGEST_PATTERN_FIRST =
      Arrays.stream(values())
          .sorted(
              Comparator.comparingInt((PlatformAccessResource r) -> r.getPathPattern().length())
                  .reversed())
          .toList();

  PlatformAccessResource(
      String pathPattern,
      String title,
      String description,
      boolean defaultAgent,
      boolean defaultClient) {
    this.pathPattern = pathPattern;
    this.title = title;
    this.description = description;
    this.defaultAgent = defaultAgent;
    this.defaultClient = defaultClient;
  }

  public boolean defaultFor(ERole role) {
    return switch (role) {
      case ADMIN -> true;
      case AGENT -> defaultAgent;
      case CLIENT -> defaultClient;
    };
  }

  public static Optional<PlatformAccessResource> resolveForPath(String servletPath) {
    for (PlatformAccessResource r : LONGEST_PATTERN_FIRST) {
      if (PATH_MATCHER.match(r.getPathPattern(), servletPath)) {
        return Optional.of(r);
      }
    }
    return Optional.empty();
  }
}
