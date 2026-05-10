package com.lemontree.applicationtracker;

import com.lemontree.applicationtracker.repository.Database;
import com.lemontree.applicationtracker.repository.JobApplicationRepository;
import com.lemontree.applicationtracker.service.ApplicationService;
import com.lemontree.applicationtracker.ui.ApplicationTrackerView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

// Einstiegspunkt der Desktop-App. JavaFX ruft die Methode start(...) auf,
// sobald das UI-System bereit ist.
public final class ApplicationTrackerApp extends Application {
    @Override
    public void start(Stage stage) {
        // Die Datenbank wird als Datei im Projektordner gespeichert.
        // So bleibt die App lokal und braucht keinen Server.
        Database database = new Database(Path.of("data", "application-tracker.db"));
        database.initialize();

        // Schichten der App:
        // Repository = Datenbankzugriff, Service = Fachlogik, View = Oberflaeche.
        JobApplicationRepository repository = new JobApplicationRepository(database);
        ApplicationService service = new ApplicationService(repository);

        // Eine Scene ist der Inhalt eines JavaFX-Fensters.
        // ApplicationTrackerView ist unser selbst gebautes Haupt-Layout.
        Scene scene = new Scene(new ApplicationTrackerView(service), 1000, 650);
        stage.setTitle("Bewerbungs-Tracker");
        stage.setMinWidth(1200);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // launch(...) startet JavaFX und fuehrt danach start(...) aus.
        launch(args);
    }
}
