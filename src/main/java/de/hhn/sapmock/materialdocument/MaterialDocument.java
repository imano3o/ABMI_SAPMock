package de.hhn.sapmock.materialdocument;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Header of a material document (A_MaterialDocumentHeader).
 * Holds what applies to the whole transaction; the posted materials are in the items.
 */
@Entity
public class MaterialDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Document number, e.g. "4900000001"; together with the year it forms the SAP key */
    @Column(nullable = false, length = 10)
    private String materialDocument;

    @Column(nullable = false, length = 4)
    private String materialDocumentYear;

    @Column(nullable = false)
    private LocalDate postingDate;

    /** Note on the transaction, e.g. patient/room; SAP allows at most 25 characters */
    @Column(length = 25)
    private String headerText;

    @OneToMany(mappedBy = "materialDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("materialDocumentItem")
    private List<MaterialDocumentItem> items = new ArrayList<>();

    protected MaterialDocument() {
    }

    public MaterialDocument(String materialDocument, String materialDocumentYear, LocalDate postingDate, String headerText) {
        this.materialDocument = materialDocument;
        this.materialDocumentYear = materialDocumentYear;
        this.postingDate = postingDate;
        this.headerText = headerText;
    }

    public void addItem(MaterialDocumentItem item) {
        item.setMaterialDocument(this);
        items.add(item);
    }

    public Long getId() {
        return id;
    }

    public String getMaterialDocument() {
        return materialDocument;
    }

    public String getMaterialDocumentYear() {
        return materialDocumentYear;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public String getHeaderText() {
        return headerText;
    }

    public List<MaterialDocumentItem> getItems() {
        return items;
    }
}
