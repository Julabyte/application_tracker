package com.lemontree.applicationtracker.repository;

import com.lemontree.applicationtracker.model.ApplicationStatus;
import com.lemontree.applicationtracker.model.JobApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Repository-Klassen kapseln Datenbankzugriffe.
// Die UI muss dadurch kein SQL kennen und kann mit Java-Objekten arbeiten.
public final class JobApplicationRepository {
    private final Database database;

    public JobApplicationRepository(Database database) {
        this.database = database;
    }

    public List<JobApplication> findAll() {
        // Text Blocks mit """ sind praktisch fuer mehrzeiliges SQL.
        String sql = """
                SELECT id, company, position, contact_name, link, applied_on, next_deadline, status, notes
                FROM job_applications
                ORDER BY COALESCE(next_deadline, '9999-12-31'), company
                """;

        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<JobApplication> applications = new ArrayList<>();
            // ResultSet ist der Cursor ueber die gefundenen Datenbankzeilen.
            while (resultSet.next()) {
                applications.add(mapRow(resultSet));
            }
            return applications;
        } catch (SQLException ex) {
            throw new IllegalStateException("Bewerbungen konnten nicht geladen werden.", ex);
        }
    }

    public JobApplication save(JobApplication application) {
        // Fragezeichen sind Platzhalter. PreparedStatement setzt die Werte später sicher ein.
        String sql = """
                INSERT INTO job_applications (
                    company, position, contact_name, link, applied_on, next_deadline, status, notes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Die Nummern 1-8 beziehen sich auf die Fragezeichen im SQL oben.
            statement.setString(1, application.company());
            statement.setString(2, application.position());
            statement.setString(3, blankToNull(application.contactName()));
            statement.setString(4, blankToNull(application.link()));
            statement.setString(5, dateToString(application.appliedOn()));
            statement.setString(6, dateToString(application.nextDeadline()));
            statement.setString(7, application.status().name());
            statement.setString(8, blankToNull(application.notes()));
            statement.executeUpdate();

            // SQLite erzeugt die ID automatisch. Hier lesen wir sie aus und geben ein vollständiges Objekt zurueck.
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new JobApplication(
                            keys.getLong(1),
                            application.company(),
                            application.position(),
                            application.contactName(),
                            application.link(),
                            application.appliedOn(),
                            application.nextDeadline(),
                            application.status(),
                            application.notes()
                    );
                }
            }

            throw new IllegalStateException("Keine ID fuer neue Bewerbung erhalten.");
        } catch (SQLException ex) {
            throw new IllegalStateException("Bewerbung konnte nicht gespeichert werden.", ex);
        }
    }

    public JobApplication update(JobApplication application) {
        String sql = """
                UPDATE job_applications SET
                    company = ?,
                    position = ?,
                    contact_name = ?,
                    link = ?,
                    applied_on = ?,
                    next_deadline = ?,
                    status = ?,
                    notes = ?
                WHERE id = ?
                """;

        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, application.company());
            statement.setString(2, application.position());
            statement.setString(3, blankToNull(application.contactName()));
            statement.setString(4, blankToNull(application.link()));
            statement.setString(5, dateToString(application.appliedOn()));
            statement.setString(6, dateToString(application.nextDeadline()));
            statement.setString(7, application.status().name());
            statement.setString(8, blankToNull(application.notes()));
            statement.setLong(9, application.id());

            int changedRows = statement.executeUpdate();
            if (changedRows == 0) {
                throw new IllegalArgumentException("Keine Bewerbung mit der ID " + application.id() + " gefunden.");
            }

            return application;
        } catch (SQLException ex) {
            throw new IllegalStateException("Bewerbung konnte nicht aktualisiert werden.", ex);
        }
    }

    public void deleteById(long id) {
        String sql = """
                DELETE FROM job_applications
                WHERE id = ?
                """;

        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Bewerbung konnte nicht gelöscht werden.", ex);
        }
    }

    private static JobApplication mapRow(ResultSet resultSet) throws SQLException {
        // Mapping bedeutet: aus einer Datenbankzeile wird ein Java-Objekt.
        return new JobApplication(
                resultSet.getLong("id"),
                resultSet.getString("company"),
                resultSet.getString("position"),
                resultSet.getString("contact_name"),
                resultSet.getString("link"),
                parseDate(resultSet.getString("applied_on")),
                parseDate(resultSet.getString("next_deadline")),
                ApplicationStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("notes")
        );
    }

    private static String blankToNull(String value) {
        // In der Datenbank speichern wir leere optionale Felder als NULL statt als leeren String.
        return value == null || value.isBlank() ? null : value;
    }

    private static String dateToString(LocalDate date) {
        // LocalDate wird in SQLite als ISO-Text gespeichert, z.B. 2026-05-10.
        return date == null ? null : date.toString();
    }

    private static LocalDate parseDate(String value) {
        // Beim Laden wird der gespeicherte Text wieder in ein LocalDate umgewandelt.
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
}
