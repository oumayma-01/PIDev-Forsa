package org.example.forsapidev.DTO;

/**
 * Référence CreditRequest minimale (utilisée dans les échéances) pour éviter les relations complexes.
 */
public class CreditRefDTO {

    private Long id;

    public CreditRefDTO() {
    }

    public CreditRefDTO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
