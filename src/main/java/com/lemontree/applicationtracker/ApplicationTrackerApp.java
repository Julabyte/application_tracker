package com.lemontree.applicationtracker;

import com.lemontree.applicationtracker.repository.Database;
import com.lemontree.applicationtracker.repository.JobApplicationRepository;
import com.lemontree.applicationtracker.service.ApplicationService;
import com.lemontree.applicationtracker.ui.ApplicationTrackerView;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Path;
import java.util.List;

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
        // Der StackPane-Root sorgt dafuer, dass die View bei DPI-/Monitorwechseln die ganze Scene fuellt.
        ApplicationTrackerView view = new ApplicationTrackerView(service);
        StackPane root = new StackPane(view);
        root.setMinSize(0, 0);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Scene scene = new Scene(root, 1180, 720);
        scene.setFill(Color.web("#f6f7f9"));
        view.prefWidthProperty().bind(scene.widthProperty());
        view.prefHeightProperty().bind(scene.heightProperty());
        scene.getStylesheets().add(ApplicationTrackerApp.class
                .getResource("/com/lemontree/applicationtracker/app.css")
                .toExternalForm());
        stage.setTitle("Bewerbungs-Tracker");
        stage.setMinWidth(920);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
        installMonitorChangeLayoutRefresh(stage, root);
    }

    public static void main(String[] args) {
        // launch(...) startet JavaFX und fuehrt danach start(...) aus.
        launch(args);
    }

    private static void installMonitorChangeLayoutRefresh(Stage stage, StackPane root) {
        Screen[] currentScreen = {findCurrentScreen(stage)};
        PauseTransition refreshDelay = new PauseTransition(Duration.millis(180));
        refreshDelay.setOnFinished(event -> {
            Screen newScreen = findCurrentScreen(stage);
            if (newScreen == currentScreen[0]) {
                return;
            }

            currentScreen[0] = newScreen;
            refreshWindowLayout(stage, root);
        });

        stage.xProperty().addListener((observable, oldValue, newValue) -> refreshDelay.playFromStart());
        stage.yProperty().addListener((observable, oldValue, newValue) -> refreshDelay.playFromStart());
    }

    private static Screen findCurrentScreen(Stage stage) {
        List<Screen> screens = Screen.getScreensForRectangle(
                stage.getX(),
                stage.getY(),
                Math.max(stage.getWidth(), 1),
                Math.max(stage.getHeight(), 1)
        );
        return screens.isEmpty() ? Screen.getPrimary() : screens.getFirst();
    }

    private static void refreshWindowLayout(Stage stage, StackPane root) {
        if (stage.isMaximized() || stage.isFullScreen() || stage.isIconified()) {
            root.requestLayout();
            return;
        }

        double width = stage.getWidth();
        root.applyCss();
        root.requestLayout();

        // Windows/JavaFX aktualisiert nach DPI-Wechseln manchmal erst nach einem echten Resize korrekt.
        stage.setWidth(width + 1);
        stage.setWidth(width);
    }
}
