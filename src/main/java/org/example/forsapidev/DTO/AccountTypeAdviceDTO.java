package org.example.forsapidev.DTO;

public class AccountTypeAdviceDTO {

    private Long    accountId;
    private String  currentType;
    private String  recommendedType;
    private boolean changeAdvised;
    private String  reasoning;

    public AccountTypeAdviceDTO() {}

    public AccountTypeAdviceDTO(Long accountId,
                                String currentType,
                                String recommendedType,
                                boolean changeAdvised,
                                String reasoning) {
        this.accountId       = accountId;
        this.currentType     = currentType;
        this.recommendedType = recommendedType;
        this.changeAdvised   = changeAdvised;
        this.reasoning       = reasoning;
    }

    public Long getAccountId()                  { return accountId; }
    public void setAccountId(Long v)            { this.accountId = v; }
    public String getCurrentType()              { return currentType; }
    public void setCurrentType(String v)        { this.currentType = v; }
    public String getRecommendedType()          { return recommendedType; }
    public void setRecommendedType(String v)    { this.recommendedType = v; }
    public boolean isChangeAdvised()            { return changeAdvised; }
    public void setChangeAdvised(boolean v)     { this.changeAdvised = v; }
    public String getReasoning()                { return reasoning; }
    public void setReasoning(String v)          { this.reasoning = v; }
}