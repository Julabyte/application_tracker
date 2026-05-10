package com.lemontree.applicationtracker.model;

// Ein enum ist eine feste Auswahl von erlaubten Werten.
// Dadurch kann der Status spaeter nicht versehentlich "irgendein String" sein.
public enum ApplicationStatus {
    DRAFT("Entwurf"),
    SENT("Beworben"),
    INTERVIEW("Gespräch"),
    OFFER("Angebot"),
    REJECTED("Absage"),
    ACCEPTED("Angenommen");

    private final String label;

    ApplicationStatus(String label) {
        // Das Label ist der Text, den wir in der Oberflaeche anzeigen.
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        // ComboBox nutzt toString(), wenn sie Werte als Text anzeigt.
        return label;
    }
}
