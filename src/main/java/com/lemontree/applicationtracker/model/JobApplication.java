package com.lemontree.applicationtracker.model;

import java.time.LocalDate;

// Ein record ist eine kompakte Datenklasse.
// Java erzeugt automatisch Konstruktor, Getter wie company(), equals(), hashCode() und toString().
public record JobApplication(
        long id,
        String company,
        String position,
        String contactName,
        String link,
        LocalDate appliedOn,
        LocalDate nextDeadline,
        ApplicationStatus status,
        String notes
) {
    // Dieser kompakte Konstruktor wird bei jeder Erstellung eines JobApplication-Objekts ausgefuehrt.
    // Hier liegen einfache Regeln, die immer gelten sollen.
    public JobApplication {
        if (company == null || company.isBlank()) {
            throw new IllegalArgumentException("Firma darf nicht leer sein.");
        }
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("Position darf nicht leer sein.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status darf nicht leer sein.");
        }
    }

    // Hilfsmethode fuer neue Einträge: id = 0 bedeutet "noch nicht in der Datenbank gespeichert".
    // Die echte ID erzeugt SQLite beim Speichern automatisch.
    public static JobApplication newEntry(
            String company,
            String position,
            String contactName,
            String link,
            LocalDate appliedOn,
            LocalDate nextDeadline,
            ApplicationStatus status,
            String notes
    ) {
        return new JobApplication(0, company, position, contactName, link, appliedOn, nextDeadline, status, notes);
    }
}
