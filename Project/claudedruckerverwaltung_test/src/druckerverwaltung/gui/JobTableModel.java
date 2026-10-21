package druckerverwaltung.gui;

import druckerverwaltung.manager.PrintManager;
import druckerverwaltung.model.PrintJob;

import javax.swing.table.AbstractTableModel;

/** Tabellenmodell, das die Aufträge direkt aus dem PrintManager liest. */
public class JobTableModel extends AbstractTableModel {

    private final PrintManager manager = PrintManager.getInstance();
    private final String[] columns = {
            "ID", "Datei", "Seiten", "Priorität", "Status", "Drucker", "Fortschritt", "Geschätzte Dauer", "Erstellt"
    };
    private int lastRowCount;

    @Override
    public int getRowCount() {
        return manager.getJobs().size();
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
    public Class<?> getColumnClass(int column) {
        return column == 0 || column == 2 || column == 6 ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        PrintJob job = getJobAt(row);
        return switch (column) {
            case 0 -> job.getId();
            case 1 -> job.getFileName();
            case 2 -> job.getPages();
            case 3 -> job.getPriority().toString();
            case 4 -> job.getStatus().toString();
            case 5 -> job.getPrinterName() != null ? job.getPrinterName() : "–";
            case 6 -> job.getProgressPercent();
            case 7 -> job.getStatus().isActive() ? Format.duration(manager.estimateDuration(job)) : "–";
            case 8 -> Format.dateTime(job.getCreatedAt());
            default -> null;
        };
    }

    public PrintJob getJobAt(int row) {
        return manager.getJobs().get(row);
    }

    /** Bei gleicher Zeilenanzahl nur die Zeilen aktualisieren, damit die Auswahl erhalten bleibt. */
    public void refresh() {
        int rows = getRowCount();
        if (rows == lastRowCount && rows > 0) {
            fireTableRowsUpdated(0, rows - 1);
        } else {
            fireTableDataChanged();
        }
        lastRowCount = rows;
    }
}
