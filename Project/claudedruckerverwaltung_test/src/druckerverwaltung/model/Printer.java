package druckerverwaltung.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Ein physischer Drucker mit eigener Prioritätswarteschlange.
 * Es gibt mehrere Drucker - auch diese Klasse ist bewusst KEIN Singleton.
 */
public class Printer {

    /** Höhere Priorität zuerst, bei gleicher Priorität der ältere Auftrag (kleinere ID). */
    private static final Comparator<PrintJob> QUEUE_ORDER =
            Comparator.comparing(PrintJob::getPriority, Comparator.reverseOrder())
                    .thenComparingInt(PrintJob::getId);

    private final int id;
    private final String name;
    private PrinterStatus status = PrinterStatus.IDLE;
    private PrintJob currentJob;
    private final int pagesPerMinute;
    private final PriorityQueue<PrintJob> queue = new PriorityQueue<>(QUEUE_ORDER);

    public Printer(int id, String name, int pagesPerMinute) {
        this.id = id;
        this.name = name;
        this.pagesPerMinute = pagesPerMinute;
    }

    public void enqueue(PrintJob job) {
        job.setPrinterName(name);
        queue.add(job);
    }

    /** Entfernt den Auftrag aus der Warteschlange bzw. bricht den laufenden Druck ab. */
    public boolean remove(PrintJob job) {
        if (job == currentJob) {
            currentJob = null;
            if (status == PrinterStatus.BUSY) {
                status = PrinterStatus.IDLE;
            }
            return true;
        }
        return queue.remove(job);
    }

    /** Muss nach einer Prioritätsänderung aufgerufen werden, damit die Warteschlange neu sortiert. */
    public void reprioritize(PrintJob job) {
        if (queue.remove(job)) {
            queue.add(job);
        }
    }

    /**
     * Simuliert den Druck für die angegebene Zeitspanne.
     *
     * @return der Auftrag, der in diesem Schritt fertig wurde, sonst null
     */
    public PrintJob process(double simulatedSeconds) {
        if (status == PrinterStatus.OFFLINE || status == PrinterStatus.ERROR) {
            return null;
        }
        if (currentJob == null) {
            currentJob = queue.poll();
            if (currentJob == null) {
                status = PrinterStatus.IDLE;
                return null;
            }
            currentJob.setStatus(Status.IN_PROGRESS);
            status = PrinterStatus.BUSY;
        }
        double pages = pagesPerMinute * simulatedSeconds / 60.0;
        if (!currentJob.addPrintedPages(pages, simulatedSeconds)) {
            return null;
        }
        PrintJob finished = currentJob;
        finished.setStatus(Status.DONE);
        currentJob = null;
        status = PrinterStatus.IDLE;
        return finished;
    }

    public boolean isIdle() {
        return status == PrinterStatus.IDLE && queue.isEmpty();
    }

    public boolean isOnline() {
        return status != PrinterStatus.OFFLINE && status != PrinterStatus.ERROR;
    }

    public void setOnline(boolean online) {
        if (online) {
            status = currentJob == null ? PrinterStatus.IDLE : PrinterStatus.BUSY;
        } else {
            status = PrinterStatus.OFFLINE;
        }
    }

    /** Geschätzte Zeit in Sekunden, bis alle Aufträge dieses Druckers abgearbeitet sind. */
    public double estimateRemainingSeconds() {
        double pages = 0;
        if (currentJob != null) {
            pages += currentJob.getPages() - currentJob.getPrintedPages();
        }
        for (PrintJob job : queue) {
            pages += job.getPages();
        }
        return secondsFor(pages);
    }

    public double secondsFor(double pages) {
        return pages * 60.0 / pagesPerMinute;
    }

    /** Warteschlange in Druckreihenfolge (Kopie). */
    public List<PrintJob> getQueue() {
        List<PrintJob> copy = new ArrayList<>(queue);
        copy.sort(QUEUE_ORDER);
        return copy;
    }

    public void clearQueue() {
        queue.clear();
        currentJob = null;
        if (isOnline()) {
            status = PrinterStatus.IDLE;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PrinterStatus getStatus() {
        return status;
    }

    public PrintJob getCurrentJob() {
        return currentJob;
    }

    public int getPagesPerMinute() {
        return pagesPerMinute;
    }

    @Override
    public String toString() {
        return name;
    }
}
