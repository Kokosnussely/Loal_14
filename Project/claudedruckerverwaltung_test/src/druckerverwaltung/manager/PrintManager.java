package druckerverwaltung.manager;

import druckerverwaltung.model.HistoryEntry;
import druckerverwaltung.model.PrintJob;
import druckerverwaltung.model.Printer;
import druckerverwaltung.model.Priority;
import druckerverwaltung.model.Status;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Zentrale Drucker- und Auftragsverwaltung - «Singleton».
 *
 * Warum gerade diese Klasse ein Singleton ist:
 * Es darf im ganzen Programm genau EINE Auftragsliste und genau EINE Verwaltung
 * der Druckerwarteschlangen geben. Gäbe es zwei PrintManager, könnte das Hauptfenster
 * einen Auftrag anlegen, den das Historienfenster nie sieht, oder zwei Verwaltungen
 * würden denselben Drucker gleichzeitig mit Aufträgen befüllen.
 *
 * Druckaufträge, Drucker und Fenster existieren dagegen mehrfach und sind daher
 * normale Klassen.
 *
 * Umsetzung:
 *  - privater Konstruktor  -> kein "new PrintManager()" von außen möglich
 *  - statisches Feld instance hält die einzige Instanz
 *  - statische Methode getInstance() erzeugt sie beim ersten Aufruf (lazy)
 */
public final class PrintManager {

    private static PrintManager instance;

    private final List<PrintJob> jobs = new ArrayList<>();
    private final List<Printer> printers = new ArrayList<>();
    private final List<HistoryEntry> history = new ArrayList<>();
    private final List<Runnable> changeListeners = new ArrayList<>();
    private int nextId = 1;
    private String filePath = "druckauftraege.txt";

    private PrintManager() {
        printers.add(new Printer(1, "Laserdrucker Büro", 30));
        printers.add(new Printer(2, "Farbdrucker", 12));
        printers.add(new Printer(3, "Plotter", 4));
    }

    /** Liefert die einzige Instanz. synchronized, damit auch bei mehreren Threads nur eine entsteht. */
    public static synchronized PrintManager getInstance() {
        if (instance == null) {
            instance = new PrintManager();
        }
        return instance;
    }

    // ---------------------------------------------------------------- Aufträge

    public PrintJob addJob(String fileName, int pages, Priority priority) {
        String name = fileName == null ? "" : fileName.replaceAll("[\\t\\r\\n]", " ").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Dateiname darf nicht leer sein");
        }
        PrintJob job = new PrintJob(nextId++, name, pages, priority);
        jobs.add(job);
        fireChanged();
        return job;
    }

    /** Löscht einen Auftrag. Ein noch aktiver Auftrag wird vorher abgebrochen. */
    public boolean removeJob(int id) {
        PrintJob job = getJob(id);
        if (job == null) {
            return false;
        }
        if (job.getStatus().isActive()) {
            cancelJob(id);
        }
        jobs.remove(job);
        fireChanged();
        return true;
    }

    /** Schickt einen wartenden Auftrag an den Drucker, der am schnellsten fertig wird. */
    public void startJob(int id) {
        PrintJob job = requireJob(id);
        if (job.getStatus() != Status.WAITING || job.getPrinterName() != null) {
            throw new IllegalStateException("Auftrag #" + id + " ist bereits gestartet oder abgeschlossen");
        }
        Printer printer = printers.stream()
                .filter(Printer::isOnline)
                .min(Comparator.comparingDouble(p -> p.estimateRemainingSeconds() + p.secondsFor(job.getPages())))
                .orElseThrow(() -> new IllegalStateException("Kein Drucker online"));
        printer.enqueue(job);
        fireChanged();
    }

    public void cancelJob(int id) {
        PrintJob job = requireJob(id);
        if (!job.getStatus().isActive()) {
            throw new IllegalStateException("Auftrag #" + id + " ist bereits abgeschlossen");
        }
        Printer printer = getPrinterOf(job);
        if (printer != null) {
            printer.remove(job);
        }
        job.setStatus(Status.CANCELLED);
        history.add(HistoryEntry.of(job));
        fireChanged();
    }

    public void setPriority(int id, Priority priority) {
        PrintJob job = requireJob(id);
        job.setPriority(priority);
        Printer printer = getPrinterOf(job);
        if (printer != null) {
            printer.reprioritize(job);
        }
        fireChanged();
    }

    public List<PrintJob> getJobs() {
        return Collections.unmodifiableList(jobs);
    }

    public PrintJob getJob(int id) {
        for (PrintJob job : jobs) {
            if (job.getId() == id) {
                return job;
            }
        }
        return null;
    }

    public Status getStatus(int id) {
        return requireJob(id).getStatus();
    }

    /**
     * Geschätzte Druckdauer in (simulierten) Sekunden: auf dem zugewiesenen Drucker die Restzeit,
     * sonst die Dauer auf dem schnellsten Drucker, der online ist.
     */
    public double estimateDuration(PrintJob job) {
        Printer printer = getPrinterOf(job);
        if (printer != null) {
            return printer.secondsFor(job.getPages() - job.getPrintedPages());
        }
        return printers.stream()
                .filter(Printer::isOnline)
                .mapToDouble(p -> p.secondsFor(job.getPages()))
                .min()
                .orElse(-1);
    }

    private PrintJob requireJob(int id) {
        PrintJob job = getJob(id);
        if (job == null) {
            throw new IllegalArgumentException("Auftrag #" + id + " existiert nicht");
        }
        return job;
    }

    // ---------------------------------------------------------------- Drucker

    public List<Printer> getPrinters() {
        return Collections.unmodifiableList(printers);
    }

    public void setPrinterOnline(Printer printer, boolean online) {
        printer.setOnline(online);
        fireChanged();
    }

    private Printer getPrinterOf(PrintJob job) {
        if (job.getPrinterName() == null || !job.getStatus().isActive()) {
            return null;
        }
        for (Printer printer : printers) {
            if (printer.getName().equals(job.getPrinterName())) {
                return printer;
            }
        }
        return null;
    }

    /** Ein Simulationsschritt: jeder Drucker druckt für die angegebene Zeit weiter. */
    public void tick(double simulatedSeconds) {
        for (Printer printer : printers) {
            PrintJob finished = printer.process(simulatedSeconds);
            if (finished != null) {
                history.add(HistoryEntry.of(finished));
            }
        }
        fireChanged();
    }

    // ---------------------------------------------------------------- Historie

    public List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
        fireChanged();
    }

    // ---------------------------------------------------------------- Speichern / Laden

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Speichert Aufträge und Historie als Textdatei (eine Zeile pro Eintrag, Tab-getrennt).
     * Dateinamen enthalten nie Tabs, das stellt addJob() sicher.
     */
    public void save() throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(Path.of(filePath), StandardCharsets.UTF_8)) {
            out.write("NEXT_ID\t" + nextId);
            out.newLine();
            for (PrintJob job : jobs) {
                out.write(String.join("\t", "JOB", String.valueOf(job.getId()), job.getFileName(),
                        String.valueOf(job.getPages()), job.getPriority().name(), job.getStatus().name(),
                        job.getCreatedAt().toString()));
                out.newLine();
            }
            for (HistoryEntry entry : history) {
                out.write(String.join("\t", "HIST", String.valueOf(entry.getJobId()), entry.getFileName(),
                        String.valueOf(entry.getPages()), entry.getEndStatus().name(),
                        entry.getCompletedAt().toString(), String.valueOf(entry.getPrinterName()),
                        String.valueOf(entry.getDurationSeconds())));
                out.newLine();
            }
        }
    }

    /**
     * Lädt Aufträge und Historie. Die Druckerwarteschlangen werden dabei geleert:
     * aktive Aufträge sind danach wieder "Wartend" und müssen neu gestartet werden.
     */
    public void load() throws IOException {
        List<PrintJob> loadedJobs = new ArrayList<>();
        List<HistoryEntry> loadedHistory = new ArrayList<>();
        int loadedNextId = 1;
        try (BufferedReader in = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] f = line.split("\t");
                try {
                    switch (f[0]) {
                        case "NEXT_ID" -> loadedNextId = Integer.parseInt(f[1]);
                        case "JOB" -> {
                            Status status = Status.valueOf(f[5]);
                            if (status.isActive()) {
                                status = Status.WAITING;
                            }
                            loadedJobs.add(new PrintJob(Integer.parseInt(f[1]), f[2], Integer.parseInt(f[3]),
                                    Priority.valueOf(f[4]), status, LocalDateTime.parse(f[6])));
                        }
                        case "HIST" -> loadedHistory.add(new HistoryEntry(Integer.parseInt(f[1]), f[2],
                                Integer.parseInt(f[3]), Status.valueOf(f[4]), LocalDateTime.parse(f[5]),
                                "null".equals(f[6]) ? null : f[6], Double.parseDouble(f[7])));
                        default -> { /* unbekannte Zeile ignorieren */ }
                    }
                } catch (RuntimeException e) {
                    throw new IOException("Ungültige Zeile in " + filePath + ": " + line, e);
                }
            }
        }
        for (Printer printer : printers) {
            printer.clearQueue();
        }
        jobs.clear();
        jobs.addAll(loadedJobs);
        history.clear();
        history.addAll(loadedHistory);
        int maxId = jobs.stream().mapToInt(PrintJob::getId).max().orElse(0);
        nextId = Math.max(loadedNextId, maxId + 1);
        fireChanged();
    }

    // ---------------------------------------------------------------- Beobachter

    /** Alle Fenster melden sich hier an und werden bei jeder Änderung benachrichtigt. */
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void fireChanged() {
        for (Runnable listener : new ArrayList<>(changeListeners)) {
            listener.run();
        }
    }
}
