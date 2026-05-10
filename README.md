# Bewerbungs-Tracker

Eine lokale Java-Desktop-App zum Üben von Java, JavaFX, SQLite und sauberer Projektstruktur.

## Stack

- Java 21
- JavaFX für die Oberfläche
- SQLite als lokale Datenbank
- Maven für Build und Abhängigkeiten

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

Falls Windows für den Installer das WiX Toolset erwartet und es nicht installiert ist, erzeugt das Script stattdessen eine portable App unter:

```text
target\app-image
```

Für einen echten `.exe`-Installer brauchst du unter Windows zusätzlich das WiX Toolset. Ohne WiX erzeugt das Script automatisch die portable Variante.

## Struktur

```text
src/main/java/com/lemontree/applicationtracker
+-- ApplicationTrackerApp.java
+-- model
+-- repository
+-- service
+-- ui
```