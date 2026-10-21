package druckerverwaltung.model;

import java.time.LocalDateTime;

/**
 * Ein einzelner Druckauftrag.
 * Druckaufträge gibt es beliebig viele - diese Klasse ist bewusst KEIN Singleton.
 */
public class PrintJob {

    private final int id;
    private final String fileName;
    private final int pages;
    private Priority priority;
    private Status status;
    private final LocalDateTime createdAt;

    /** Bereits gedruckte Seiten (Kommazahl, weil pro Simulationsschritt Teilseiten anfallen). */
    private double printedPages;
    /** Simulierte Sekunden, die der Auftrag bisher im Drucker verbracht hat. */
    private double printSeconds;
    /** Name des Druckers, dem der Auftrag zugewiesen ist (oder null). */
    private String printerName;

    public PrintJob(int id, String fileName, int pages, Priority priority) {
        this(id, fileName, pages, priority, Status.WAITING, LocalDateTime.now());
    }

    public PrintJob(int id, String fileName, int pages, Priority priority, Status status, LocalDateTime createdAt) {
        if (pages <= 0) {
            throw new IllegalArgumentException("Seitenanzahl muss größer als 0 sein");
        }
        this.id = id;
        this.fileName = fileName;
        this.pages = pages;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPages() {
        return pages;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getPrintedPages() {
        return printedPages;
    }

    public double getPrintSeconds() {
        return printSeconds;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    /** Druckt weitere Seiten und gibt true zurück, sobald der Auftrag vollständig gedruckt ist. */
    public boolean addPrintedPages(double newPages, double seconds) {
        printedPages = Math.min(pages, printedPages + newPages);
        printSeconds += seconds;
        return printedPages >= pages;
    }

    /** Setzt den Druckfortschritt zurück (z.B. nach dem Laden aus einer Datei). */
    public void resetProgress() {
        printedPages = 0;
        printSeconds = 0;
        printerName = null;
    }

    public int getProgressPercent() {
        return (int) (printedPages * 100 / pages);
    }

    @Override
    public String toString() {
        return "#" + id + " " + fileName + " (" + pages + " S., " + priority + ", " + status + ")";
    }
}
