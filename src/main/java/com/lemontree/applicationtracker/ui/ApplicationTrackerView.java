package com.lemontree.applicationtracker.ui;

import com.lemontree.applicationtracker.model.ApplicationStatus;
import com.lemontree.applicationtracker.model.JobApplication;
import com.lemontree.applicationtracker.service.ApplicationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

// Diese Klasse baut die komplette sichtbare Oberfläche.
// Sie erbt von BorderPane, einem Layout mit Bereichen: oben, links, mitte, rechts, unten.
public final class ApplicationTrackerView extends BorderPane {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final double FORM_LABEL_WIDTH = 105;

    private final ApplicationService service;

    // ObservableList informiert die TableView automatisch, wenn sich der Inhalt ändert.
    private final ObservableList<JobApplication> applications = FXCollections.observableArrayList();

    // Diese Felder sind UI-Komponenten, aus denen wir beim Speichern die Eingaben lesen.
    private final TextField companyField = new TextField();
    private final TextField positionField = new TextField();
    private final TextField contactField = new TextField();
    private final TextField linkField = new TextField();
    private final DatePicker appliedOnPicker = new DatePicker();
    private final DatePicker deadlinePicker = new DatePicker();
    private final ComboBox<ApplicationStatus> statusBox = new ComboBox<>();
    private final TextArea notesArea = new TextArea();
    private TableView<JobApplication> applicationTable;
    private VBox detailsForm;
    private Label formTitle;
    private Label formHint;
    private Button deleteButton;
    private Button showCompletedButton;
    private JobApplication editedApplication;
    private boolean showCompletedApplications;

    public ApplicationTrackerView(ApplicationService service) {
        this.service = service;

        // BorderPane-Aufteilung: Kopf oben, Tabelle in der Mitte, Formular rechts.
        setPadding(new Insets(18));
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        getStyleClass().add("app-shell");
        setTop(createHeader());
        setCenter(createContent());
        detailsForm = createForm();

        // Direkt beim Start vorhandene Daten aus der Datenbank laden.
        refreshApplications();
    }

    private HBox createHeader() {
        // HBox und VBox sind Layout-Container:
        // HBox legt Kinder horizontal, VBox vertikal an.
        Label title = new Label("Bewerbungs-Tracker");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Lokale JavaFX-App mit SQLite");
        subtitle.getStyleClass().add("app-subtitle");

        VBox texts = new VBox(3, title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newApplicationButton = new Button("Neue Bewerbung");
        newApplicationButton.getStyleClass().add("primary-button");
        newApplicationButton.setOnAction(event -> startNewApplication());

        showCompletedButton = new Button();
        showCompletedButton.getStyleClass().add("secondary-button");
        showCompletedButton.setOnAction(event -> toggleCompletedApplications());
        updateCompletedButtonText();

        HBox header = new HBox(16, texts, spacer, showCompletedButton, newApplicationButton);
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("app-header");
        header.setPadding(new Insets(0, 0, 14, 0));
        return header;
    }

    private StackPane createContent() {
        TableView<JobApplication> table = createTable();

        Label emptyTitle = new Label("Noch keine Bewerbungen");
        emptyTitle.getStyleClass().add("empty-state-title");
        Label emptyText = new Label("Lege oben rechts deine erste Bewerbung an.");
        emptyText.getStyleClass().add("empty-state-text");
        VBox emptyState = new VBox(8, emptyTitle, emptyText);
        emptyState.getStyleClass().add("empty-state");

        table.setPlaceholder(emptyState);
        StackPane content = new StackPane(table);
        content.setMinSize(0, 0);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return content;
    }

    private TableView<JobApplication> createTable() {
        // Die Tabelle zeigt die ObservableList applications an.
        TableView<JobApplication> table = new TableView<>(applications);
        applicationTable = table;
        table.setMinSize(0, 0);
        table.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Jede Spalte bekommt eine Funktion, die aus JobApplication den passenden Text holt.
        TableColumn<JobApplication, String> companyColumn = column("Firma", JobApplication::company);
        TableColumn<JobApplication, String> positionColumn = column("Position", JobApplication::position);
        TableColumn<JobApplication, String> statusColumn = column("Status", item -> item.status().label());
        TableColumn<JobApplication, String> appliedOnColumn = column("Beworben am", item -> formatDate(item.appliedOn()));
        TableColumn<JobApplication, String> deadlineColumn = column("Deadline", item -> formatDate(item.nextDeadline()));
        TableColumn<JobApplication, String> contactColumn = column("Kontakt", JobApplication::contactName);

        table.getColumns().addAll(companyColumn, positionColumn, statusColumn, appliedOnColumn, deadlineColumn, contactColumn);

        table.setRowFactory(tv -> {
            TableRow<JobApplication> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    startEditingApplication(row.getItem());
                }
            });
            return row;
        });

        return table;
    }

    private VBox createForm() {
        // PromptText ist ein Platzhalter, solange das Feld leer ist.
        companyField.setPromptText("z.B. LemonTree GmbH");
        positionField.setPromptText("z.B. Junior Java Developer");
        contactField.setPromptText("Name oder E-Mail");
        linkField.setPromptText("Stellenanzeige oder Karriereseite");
        statusBox.setItems(FXCollections.observableArrayList(ApplicationStatus.values()));
        statusBox.setValue(ApplicationStatus.DRAFT);
        notesArea.setPromptText("Notizen zum Stand, Fragen, nächste Schritte ...");
        notesArea.setPrefRowCount(5);

        // GridPane eignet sich fuer Formular-Layouts: Label links, Eingabefeld rechts.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, formLabel("Firma"), companyField);
        grid.addRow(1, formLabel("Position"), positionField);
        grid.addRow(2, formLabel("Kontakt"), contactField);
        grid.addRow(3, formLabel("Link"), linkField);
        grid.addRow(4, formLabel("Beworben am"), appliedOnPicker);
        grid.addRow(5, formLabel("Frist/Termin"), deadlinePicker);
        grid.addRow(6, formLabel("Status"), statusBox);
        grid.addRow(7, formLabel("Notizen"), notesArea);

        // Hgrow erlaubt den Eingabefeldern, verfuegbaren horizontalen Platz mitzunehmen.
        GridPane.setHgrow(companyField, Priority.ALWAYS);
        GridPane.setHgrow(positionField, Priority.ALWAYS);
        GridPane.setHgrow(contactField, Priority.ALWAYS);
        GridPane.setHgrow(linkField, Priority.ALWAYS);
        GridPane.setHgrow(notesArea, Priority.ALWAYS);

        Button saveButton = new Button("Speichern");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.getStyleClass().add("primary-button");
        // setOnAction registriert Code, der beim Klick auf den Button ausgefuehrt wird.
        saveButton.setOnAction(event -> saveApplication());

        Button clearFormButton = new Button("Zuruecksetzen");
        clearFormButton.setMaxWidth(Double.MAX_VALUE);
        clearFormButton.getStyleClass().add("secondary-button");
        clearFormButton.setOnAction(event -> clearForm());

        deleteButton = new Button("Bewerbung löschen");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(event -> deleteEditedApplication());

        formTitle = new Label("Neue Bewerbung");
        formTitle.getStyleClass().add("detail-title");
        formHint = new Label("Erfasse die wichtigsten Eckdaten und speichere danach den Eintrag.");
        formHint.setWrapText(true);
        formHint.getStyleClass().add("detail-hint");

        HBox actions = new HBox(10, saveButton, clearFormButton);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        HBox.setHgrow(clearFormButton, Priority.ALWAYS);

        VBox form = new VBox(14, formTitle, formHint, grid, actions, deleteButton);
        form.getStyleClass().add("detail-panel");
        BorderPane.setMargin(form, new Insets(0, 0, 0, 18));
        form.setPrefWidth(430);
        return form;
    }

    private void saveApplication() {
        try {
            // Aus den Eingabefeldern wird ein Datenobjekt gebaut.
            JobApplication application = new JobApplication(
                    editedApplication == null ? 0 : editedApplication.id(),
                    companyField.getText(),
                    positionField.getText(),
                    contactField.getText(),
                    linkField.getText(),
                    appliedOnPicker.getValue(),
                    deadlinePicker.getValue(),
                    statusBox.getValue(),
                    notesArea.getText()
            );

            // Speichern läuft durch den Service. Danach wird das Formular geleert und die Tabelle neu geladen.
            if (editedApplication == null) {
                service.addApplication(application);
            } else {
                service.updateApplication(application);
            }
            closeDetailsForm();
            refreshApplications();
        } catch (IllegalArgumentException ex) {
            showError("Eingabe pruefen", ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Speichern fehlgeschlagen", ex.getMessage());
        }
    }

    private void refreshApplications() {
        // setAll ersetzt den aktuellen Tabelleninhalt durch die frisch geladenen Daten.
        if (showCompletedApplications) {
            applications.setAll(service.listApplications());
        } else {
            applications.setAll(service.getOpenApplications());
        }
    }

    private void toggleCompletedApplications() {
        showCompletedApplications = !showCompletedApplications;
        updateCompletedButtonText();
        refreshApplications();
    }

    private void updateCompletedButtonText() {
        if (showCompletedApplications) {
            showCompletedButton.setText("Abgeschlossene ausblenden");
        } else {
            showCompletedButton.setText("Abgeschlossene anzeigen");
        }
    }

    private void startNewApplication() {
        editedApplication = null;
        applicationTable.getSelectionModel().clearSelection();
        formTitle.setText("Neue Bewerbung");
        formHint.setText("Erfasse die wichtigsten Eckdaten und speichere danach den Eintrag.");
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
        clearForm();
        setRight(detailsForm);
    }

    private void startEditingApplication(JobApplication application) {
        editedApplication = application;
        formTitle.setText("Bewerbung bearbeiten");
        formHint.setText(application.company() + " - " + application.position());
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
        loadApplicationIntoForm(application);
        setRight(detailsForm);
    }

    private void closeDetailsForm() {
        clearForm();
        editedApplication = null;
        applicationTable.getSelectionModel().clearSelection();
        setRight(null);
    }

    private void clearForm() {
        // Alle Eingabefelder wieder in ihren Startzustand bringen.
        companyField.clear();
        positionField.clear();
        contactField.clear();
        linkField.clear();
        appliedOnPicker.setValue(null);
        deadlinePicker.setValue(null);
        statusBox.setValue(ApplicationStatus.DRAFT);
        notesArea.clear();
    }

    private void deleteEditedApplication() {
        if (editedApplication == null) {
            showError("Keine Bewerbung", "Es ist keine bestehende Bewerbung zum Löschen geöffnet.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Bewerbung löschen");
        confirmation.setHeaderText("Bewerbung löschen?");
        confirmation.setContentText(editedApplication.company() + " - " + editedApplication.position());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        service.deleteApplication(editedApplication.id());
        closeDetailsForm();
        refreshApplications();
    }

    private void loadApplicationIntoForm(JobApplication application) {
        companyField.setText(nullToEmpty(application.company()));
        positionField.setText(nullToEmpty(application.position()));
        contactField.setText(nullToEmpty(application.contactName()));
        linkField.setText(nullToEmpty(application.link()));
        appliedOnPicker.setValue(application.appliedOn());
        deadlinePicker.setValue(application.nextDeadline());
        statusBox.setValue(application.status());
        notesArea.setText(nullToEmpty(application.notes()));
    }

    private static Label formLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        label.setMinWidth(FORM_LABEL_WIDTH);
        label.setPrefWidth(FORM_LABEL_WIDTH);
        return label;
    }

    private static TableColumn<JobApplication, String> column(String title, ValueExtractor extractor) {
        TableColumn<JobApplication, String> column = new TableColumn<>(title);
        // JavaFX-Tabellen erwarten Properties. SimpleStringProperty verpackt unseren Text passend.
        column.setCellValueFactory(data -> new SimpleStringProperty(nullToEmpty(extractor.extract(data.getValue()))));
        return column;
    }

    private static String formatDate(java.time.LocalDate date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static void showError(String title, String message) {
        // Alert ist ein kleines Dialogfenster fuer Fehler oder Hinweise.
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface ValueExtractor {
        // Kleine eigene Schnittstelle, damit column(...) flexibel angeben kann,
        // welcher Wert aus einer Bewerbung angezeigt werden soll.
        String extract(JobApplication application);
    }
}
