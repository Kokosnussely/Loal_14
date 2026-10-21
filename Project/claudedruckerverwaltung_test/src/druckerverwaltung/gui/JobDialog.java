package druckerverwaltung.gui;

import druckerverwaltung.manager.PrintManager;
import druckerverwaltung.model.PrintJob;
import druckerverwaltung.model.Priority;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** Dialog zum Anlegen eines neuen Druckauftrags. */
public class JobDialog extends JDialog {

    // Der Dialog holt sich selbst die Instanz - er bekommt sie nicht übergeben und erzeugt keine neue.
    private final PrintManager manager = PrintManager.getInstance();
    private final JTextField fileNameField = new JTextField(20);
    private final JSpinner pagesField = new JSpinner(new SpinnerNumberModel(1, 1, 10_000, 1));
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JCheckBox startNowBox = new JCheckBox("Sofort drucken", true);

    public JobDialog(JFrame owner) {
        super(owner, "Neuer Druckauftrag", true);
        priorityBox.setSelectedItem(Priority.NORMAL);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Dateiname:"));
        form.add(fileNameField);
        form.add(new JLabel("Seiten:"));
        form.add(pagesField);
        form.add(new JLabel("Priorität:"));
        form.add(priorityBox);
        form.add(new JLabel());
        form.add(startNowBox);

        JButton ok = new JButton("Anlegen");
        JButton cancel = new JButton("Abbrechen");
        ok.addActionListener(e -> create());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        pack();
        setLocationRelativeTo(owner);
    }

    private void create() {
        try {
            PrintJob job = manager.addJob(fileNameField.getText(), (Integer) pagesField.getValue(),
                    (Priority) priorityBox.getSelectedItem());
            if (startNowBox.isSelected()) {
                manager.startJob(job.getId());
            }
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }
}
