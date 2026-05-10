# Bewerbungs-Tracker

Eine lokale Java-Desktop-App zum Ueben von Java, JavaFX, SQLite und sauberer Projektstruktur.

## Stack

- Java 21
- JavaFX fuer die Oberflaeche
- SQLite als lokale Datenbank
- Maven fuer Build und Abhaengigkeiten

## Starten

```powershell
mvn javafx:run
```

Beim ersten Start wird die lokale Datenbank unter `data\application-tracker.db` angelegt.

## Struktur

```text
src/main/java/com/lemontree/applicationtracker
+-- ApplicationTrackerApp.java
+-- model
+-- repository
+-- service
+-- ui
```

## Uebungs-TODOs

Im Code findest du mehrere `TODO Uebung`-Kommentare. Sinnvolle erste Aufgaben:

- Validierung ergaenzen: Deadline darf nicht vor dem Bewerbungsdatum liegen.
- Formular zuruecksetzen: Button in der UI einbauen.
- Bestehende Bewerbung bearbeiten: `update` im Repository implementieren.
- Bewerbung loeschen: `deleteById` im Repository und Button in der UI.
- Tabelle interaktiv machen: Doppelklick laedt einen Eintrag ins Formular.
