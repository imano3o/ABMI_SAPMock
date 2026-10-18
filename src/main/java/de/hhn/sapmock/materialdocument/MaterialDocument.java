package de.hhn.sapmock.materialdocument;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class MaterialDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materialDocument;

    private String materialDocumentYear;

    private String postingDate;

    private String headerText;

    private String goodsMovementCode;

    private List<MaterialDocumentItem> items;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
