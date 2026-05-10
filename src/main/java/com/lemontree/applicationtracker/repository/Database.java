package com.lemontree.applicationtracker.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Diese Klasse kennt die Datenbankdatei und bereitet die Tabellen vor.
// Andere Klassen muessen dadurch nicht wissen, wo die SQLite-Datei liegt.
public final class Database {
    private final Path databasePath;

    public Database(Path databasePath) {
        this.databasePath = databasePath;
    }

    public void initialize() {
        try {
            // Falls der Ordner data/ noch nicht existiert, wird er hier angelegt.
            Path parent = databasePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            // try-with-resources schliesst Connection und Statement automatisch.
            try (Connection connection = openConnection();
                 Statement statement = connection.createStatement()) {
                // CREATE TABLE IF NOT EXISTS ist sicher mehrfach ausfuehrbar:
                // Beim ersten Start wird die Tabelle erzeugt, danach bleibt sie unverändert.
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS job_applications (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            company TEXT NOT NULL,
                            position TEXT NOT NULL,
                            contact_name TEXT,
                            link TEXT,
                            applied_on TEXT,
                            next_deadline TEXT,
                            status TEXT NOT NULL,
                            notes TEXT
                        )
                        """);
            }
        } catch (IOException | SQLException ex) {
            throw new IllegalStateException("Datenbank konnte nicht vorbereitet werden.", ex);
        }
    }

    Connection openConnection() throws SQLException {
        // JDBC ist die Standard-API, mit der Java auf Datenbanken zugreift.
        // Der sqlite-jdbc-Treiber versteht diese URL und oeffnet die lokale Datei.
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
    }
}
