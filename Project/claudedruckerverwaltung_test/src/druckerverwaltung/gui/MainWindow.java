package druckerverwaltung.gui;

import druckerverwaltung.manager.PrintManager;
import druckerverwaltung.model.PrintJob;
import druckerverwaltung.model.Priority;
import druckerverwaltung.model.Status;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/** Hauptfenster: Liste aller Druckaufträge und Bedienelemente. */
public class MainWindow extends JFrame {

    /** Ein Timer-Schritt (1 echte Sekunde) entspricht so vielen simulierten Sekunden. */
    private static final double SIMULATED_SECONDS_PER_TICK = 10;

    private final PrintManager manager = PrintManager.getInstance();
    private final JobTableModel tableModel = new JobTableModel();
    private final JTable jobTable = new JTable(tableModel);
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JButton startButton = new JButton("Starten");
    private final JButton cancelButton = new JButton("Abbrechen");
    private final JButton deleteButton = new JButton("Löschen");
    private final JButton priorityButton = new JButton("Priorität setzen");

    public MainWindow() {
        super("Druckerverwaltung");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        priorityBox.setMaximumSize(priorityBox.getPreferredSize());
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobTable.getColumnModel().getColumn(6).setCellRenderer(progressRenderer());
        jobTable.getSelectionModel().addListSelectionListener(e -> updateButtons());

        JButton newButton = new JButton("Neuer Auftrag");
        JButton saveButton = new JButton("Speichern");
        JButton loadButton = new JButton("Laden");
        JButton historyButton = new JButton("Historie & Statistik");

        newButton.addActionListener(e -> new JobDialog(this).setVisible(true));
        startButton.addActionListener(e -> withSelectedJob(job -> manager.startJob(job.getId())));
        cancelButton.addActionListener(e -> withSelectedJob(job -> manager.cancelJob(job.getId())));
        deleteButton.addActionListener(e -> withSelectedJob(job -> manager.removeJob(job.getId())));
        priorityButton.addActionListener(e -> withSelectedJob(
                job -> manager.setPriority(job.getId(), (Priority) priorityBox.getSelectedItem())));
        saveButton.addActionListener(e -> save());
        loadButton.addActionListener(e -> load());
        historyButton.addActionListener(e -> {
            HistoryWindow window = new HistoryWindow();
            window.setLocationRelativeTo(this);
            window.setVisible(true);
        });

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(newButton);
        toolBar.add(startButton);
        toolBar.add(cancelButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator();
        toolBar.add(new JLabel("Priorität: "));
        toolBar.add(priorityBox);
        toolBar.add(priorityButton);
        toolBar.addSeparator();
        toolBar.add(saveButton);
        toolBar.add(loadButton);
        toolBar.add(historyButton);

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(jobTable), BorderLayout.CENTER);
        add(new StatusPanel(), BorderLayout.SOUTH);

        manager.addChangeListener(this::refresh);
        new Timer(1000, e -> manager.tick(SIMULATED_SECONDS_PER_TICK)).start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        refresh();
        setSize(1100, 550);
        setLocationRelativeTo(null);
    }

    public void refresh() {
        tableModel.refresh();
        updateButtons();
    }

    private void updateButtons() {
        PrintJob job = getSelectedJob();
        boolean selected = job != null;
        startButton.setEnabled(selected && job.getStatus() == Status.WAITING && job.getPrinterName() == null);
        cancelButton.setEnabled(selected && job.getStatus().isActive());
        deleteButton.setEnabled(selected);
        priorityButton.setEnabled(selected && job.getStatus().isActive());
    }

    private PrintJob getSelectedJob() {
        int row = jobTable.getSelectedRow();
        return row < 0 ? null : tableModel.getJobAt(jobTable.convertRowIndexToModel(row));
    }

    private void withSelectedJob(java.util.function.Consumer<PrintJob> action) {
        PrintJob job = getSelectedJob();
        if (job == null) {
            return;
        }
        try {
            action.accept(job);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void save() {
        try {
            manager.save();
            JOptionPane.showMessageDialog(this, "Gespeichert in " + manager.getFilePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Speichern fehlgeschlagen: " + ex.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void load() {
        try {
            manager.load();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Laden fehlgeschlagen: " + ex.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exit() {
        int answer = JOptionPane.showConfirmDialog(this, "Aufträge vor dem Beenden speichern?",
                "Beenden", JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
            return;
        }
        if (answer == JOptionPane.YES_OPTION) {
            try {
                manager.save();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Speichern fehlgeschlagen: " + ex.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        System.exit(0);
    }

    private static TableCellRenderer progressRenderer() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        return (table, value, isSelected, hasFocus, row, column) -> {
            bar.setValue((Integer) value);
            return bar;
        };
    }
}
