package druckerverwaltung.model;

import java.util.List;

/** Wertet die Druckhistorie aus. */
public class Statistics {

    private int totalJobs;
    private int totalPages;
    private int cancelled;
    private double averageDuration;

    public void calculate(List<HistoryEntry> history) {
        totalJobs = history.size();
        totalPages = 0;
        cancelled = 0;
        double durationSum = 0;
        int done = 0;
        for (HistoryEntry entry : history) {
            if (entry.getEndStatus() == Status.DONE) {
                totalPages += entry.getPages();
                durationSum += entry.getDurationSeconds();
                done++;
            } else if (entry.getEndStatus() == Status.CANCELLED) {
                cancelled++;
            }
        }
        averageDuration = done == 0 ? 0 : durationSum / done;
    }

    public int getTotalJobs() {
        return totalJobs;
    }

    /** Anzahl der tatsächlich gedruckten Seiten (nur fertige Aufträge). */
    public int getTotalPages() {
        return totalPages;
    }

    public int getCancelled() {
        return cancelled;
    }

    /** Durchschnittliche (simulierte) Druckdauer fertiger Aufträge in Sekunden. */
    public double getAverageDuration() {
        return averageDuration;
    }
}
