package org.example.forsapidev.security.nav;

import lombok.Getter;
import org.example.forsapidev.entities.UserManagement.ERole;

/**
 * Sidebar entries aligned with Angular ({@code dashboard-sidebar.component.ts}).
 * {@link #frontendPath} is stored for role-based navigation.
 */
@Getter
public enum DashboardNavFeature {
  DASHBOARD("/dashboard", "Dashboard", "Overview of your dashboard home.", false),
  PROFILE("/dashboard/profile", "My profile", "Account profile and settings.", false),
  CREDIT("/dashboard/credit", "Credit Management", "Credit requests and follow-up.", false),
  WALLET("/dashboard/wallet", "Digital Wallet", "Wallet balance and transactions.", false),
  INSURANCE("/dashboard/insurance", "Insurance", "Products, policies, and claims.", false),
  PARTENARIAT("/dashboard/partenariat", "Partnerships", "Partners and offers.", false),
  SCORING("/dashboard/scoring", "My score", "Personal credit score and document checks.", false),
  AI_SCORE("/dashboard/ai-score", "My score", "Personal credit score and document checks.", false),
  FEEDBACK("/dashboard/feedback", "Feedback", "Complaints, feedback, and assistant.", false),
  AI("/dashboard/ai", "AI Risk Analysis", "AI-assisted risk analysis.", false),
  USERS("/dashboard/users", "User management", "Manage user accounts.", true),
  ROLES("/dashboard/roles", "Role management", "Roles and sidebar access for each role.", true);

  private final String frontendPath;
  private final String title;
  private final String description;
  /** When true, off by default for non-admin roles (can still be enabled per role). */
  private final boolean adminOnlyDefault;

  DashboardNavFeature(String frontendPath, String title, String description, boolean adminOnlyDefault) {
    this.frontendPath = frontendPath;
    this.title = title;
    this.description = description;
    this.adminOnlyDefault = adminOnlyDefault;
  }

  public String getCode() {
    return name();
  }

  public boolean defaultPermittedFor(ERole role) {
    if (role == ERole.ADMIN) {
      return true;
    }
    if (adminOnlyDefault) {
      return false;
    }
    return role == ERole.AGENT || role == ERole.CLIENT;
  }

  public static DashboardNavFeature fromCode(String code) {
    return valueOf(code);
  }
}
