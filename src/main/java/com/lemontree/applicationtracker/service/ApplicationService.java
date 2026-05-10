package com.lemontree.applicationtracker.service;

import com.lemontree.applicationtracker.model.ApplicationStatus;
import com.lemontree.applicationtracker.model.JobApplication;
import com.lemontree.applicationtracker.repository.JobApplicationRepository;

import java.time.LocalDate;
import java.util.List;

// Die Service-Schicht ist der Platz fuer Fachlogik.
// Aktuell leitet sie viel nur ans Repository weiter, spaeter kommen hier Regeln hinein.
public final class ApplicationService {
    private final JobApplicationRepository repository;

    public ApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public List<JobApplication> listApplications() {
        // Die UI fragt den Service nach Daten, nicht direkt das Repository.
        return repository.findAll();
    }

    public JobApplication addApplication(JobApplication application) {
        // Diese Methode ist ein guter Ort fuer Regeln, die nicht nur ein einzelnes Feld betreffen.
        validateApplication(application);
        return repository.save(application);
    }

    public JobApplication updateApplication(JobApplication application) {
        validateApplication(application);
        return repository.update(application);
    }

    private static void validateApplication(JobApplication application) {
        LocalDate deadlineDate = application.nextDeadline();
        LocalDate appliedOn = application.appliedOn();
        if (deadlineDate != null && appliedOn != null && deadlineDate.isBefore(appliedOn)) {
            throw new IllegalArgumentException("Die Deadline darf nicht vor dem Bewerbungsdatum liegen.");
        }
    }

    public void deleteApplication(long id) {
        repository.deleteById(id);
    }

    public List<JobApplication> getOpenApplications() {
        return repository.findAll().stream()
                .filter(app -> app.status() != ApplicationStatus.REJECTED && app.status() != ApplicationStatus.ACCEPTED)
                .toList();
    }
}
