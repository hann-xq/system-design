package nl.saxion.Models.observer;

public interface DashboardObserver {
    void updateSpoolChanges(int spoolChanges);
    void updatePrintsFulfilled(int printsFulfilled);
}

