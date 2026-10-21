package druckerverwaltung.model;

import java.time.LocalDateTime;

/** Eintrag in der Druckhistorie - wird angelegt, wenn ein Auftrag fertig oder abgebrochen ist. */
public class HistoryEntry {

    private final int jobId;
    private final String fileName;
    private final int pages;
    private final Status endStatus;
    private final LocalDateTime completedAt;
    private final String printerName;
    private final double durationSeconds;

    public HistoryEntry(int jobId, String fileName, int pages, Status endStatus,
                        LocalDateTime completedAt, String printerName, double durationSeconds) {
        this.jobId = jobId;
        this.fileName = fileName;
        this.pages = pages;
        this.endStatus = endStatus;
        this.completedAt = completedAt;
        this.printerName = printerName;
        this.durationSeconds = durationSeconds;
    }

    public static HistoryEntry of(PrintJob job) {
        return new HistoryEntry(job.getId(), job.getFileName(), job.getPages(), job.getStatus(),
                LocalDateTime.now(), job.getPrinterName(), job.getPrintSeconds());
    }

    public int getJobId() {
        return jobId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPages() {
        return pages;
    }

    public Status getEndStatus() {
        return endStatus;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getPrinterName() {
        return printerName;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }
}
