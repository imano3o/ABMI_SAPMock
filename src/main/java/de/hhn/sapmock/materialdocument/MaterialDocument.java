package de.hhn.sapmock.materialdocument;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MaterialDocument {
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
