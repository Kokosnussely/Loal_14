package druckerverwaltung.gui;

import druckerverwaltung.manager.PrintManager;
import druckerverwaltung.model.HistoryEntry;
import druckerverwaltung.model.Statistics;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Eigenes Fenster für Druckhistorie und Statistiken.
 * Es greift auf DIESELBE PrintManager-Instanz zu wie das Hauptfenster.
 */
public class HistoryWindow extends JFrame {

    private final PrintManager manager = PrintManager.getInstance();
    private final Statistics statistics = new Statistics();
    private final HistoryTableModel tableModel = new HistoryTableModel();
    private final JLabel statisticsLabel = new JLabel();
    private final Runnable listener = this::refresh;

    public HistoryWindow() {
        super("Druckhistorie & Statistik");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JButton clear = new JButton("Historie leeren");
        clear.addActionListener(e -> manager.clearHistory());
        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        south.add(statisticsLabel, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(clear);
        south.add(buttons, BorderLayout.EAST);

        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        manager.addChangeListener(listener);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                manager.removeChangeListener(listener);
            }
        });
        refresh();
        setSize(800, 400);
    }

    private void refresh() {
        statistics.calculate(manager.getHistory());
        statisticsLabel.setText(String.format(
                "Aufträge gesamt: %d | gedruckte Seiten: %d | abgebrochen: %d | Ø Druckdauer: %s",
                statistics.getTotalJobs(), statistics.getTotalPages(), statistics.getCancelled(),
                Format.duration(statistics.getAverageDuration())));
        tableModel.fireTableDataChanged();
    }

    private class HistoryTableModel extends AbstractTableModel {

        private final String[] columns = {"Auftrag", "Datei", "Seiten", "Ergebnis", "Drucker", "Druckdauer", "Abgeschlossen"};

        @Override
        public int getRowCount() {
            return manager.getHistory().size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            // neueste Einträge oben
            List<HistoryEntry> history = manager.getHistory();
            HistoryEntry e = history.get(history.size() - 1 - row);
            return switch (column) {
                case 0 -> "#" + e.getJobId();
                case 1 -> e.getFileName();
                case 2 -> e.getPages();
                case 3 -> e.getEndStatus().toString();
                case 4 -> e.getPrinterName() == null ? "–" : e.getPrinterName();
                case 5 -> Format.duration(e.getDurationSeconds());
                case 6 -> Format.dateTime(e.getCompletedAt());
                default -> null;
            };
        }
    }
}
