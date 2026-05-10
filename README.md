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

Beim ersten Start wird die lokale Datenbank im Benutzerprofil angelegt, unter Windows z.B.:

```text
%LOCALAPPDATA%\BewerbungsTracker\application-tracker.db
```

## Windows-EXE bauen

```powershell
.\scripts\package-windows.ps1
```

Das Script nutzt Maven und `jpackage` aus deinem JDK. Wenn ein Installer gebaut werden kann, findest du ihn unter:

```text
target\installer
```

Falls Windows fuer den Installer das WiX Toolset erwartet und es nicht installiert ist, erzeugt das Script stattdessen eine portable App unter:

```text
target\app-image
```

Fuer einen echten `.exe`-Installer brauchst du unter Windows zusaetzlich das WiX Toolset. Ohne WiX erzeugt das Script automatisch die portable Variante.

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
