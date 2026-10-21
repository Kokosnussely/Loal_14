package druckerverwaltung.gui;

import druckerverwaltung.manager.PrintManager;
import druckerverwaltung.model.PrintJob;
import druckerverwaltung.model.Printer;
import druckerverwaltung.model.Status;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Zeigt den Zustand aller Drucker und eine Zusammenfassung der Aufträge. */
public class StatusPanel extends JPanel {

    private final PrintManager manager = PrintManager.getInstance();
    private final JLabel statusLabel = new JLabel();
    private final List<JLabel> printerLabels = new ArrayList<>();

    public StatusPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Druckstatus"));

        JPanel printerPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        for (Printer printer : manager.getPrinters()) {
            JLabel label = new JLabel();
            printerLabels.add(label);
            JCheckBox online = new JCheckBox("Online", printer.isOnline());
            online.addActionListener(e -> manager.setPrinterOnline(printer, online.isSelected()));
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.add(label, BorderLayout.CENTER);
            row.add(online, BorderLayout.EAST);
            printerPanel.add(row);
        }
        add(printerPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        manager.addChangeListener(this::refresh);
        refresh();
    }

    public void refresh() {
        List<Printer> printers = manager.getPrinters();
        for (int i = 0; i < printers.size(); i++) {
            Printer p = printers.get(i);
            PrintJob current = p.getCurrentJob();
            String text = p.getName() + " (" + p.getPagesPerMinute() + " S./min): " + p.getStatus();
            if (current != null) {
                text += " – " + current.getFileName() + " " + current.getProgressPercent() + " %";
            }
            text += " | Warteschlange: " + p.getQueue().size()
                    + " | frei in " + Format.duration(p.estimateRemainingSeconds());
            printerLabels.get(i).setText(text);
        }

        int waiting = 0;
        int printing = 0;
        int done = 0;
        int cancelled = 0;
        for (PrintJob job : manager.getJobs()) {
            if (job.getStatus() == Status.WAITING) {
                waiting++;
            } else if (job.getStatus() == Status.IN_PROGRESS) {
                printing++;
            } else if (job.getStatus() == Status.DONE) {
                done++;
            } else {
                cancelled++;
            }
        }
        statusLabel.setText(String.format("Aufträge: %d wartend, %d im Druck, %d fertig, %d abgebrochen",
                waiting, printing, done, cancelled));
    }
}
