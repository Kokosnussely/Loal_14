package druckerverwaltung;

import druckerverwaltung.gui.MainWindow;
import druckerverwaltung.manager.PrintManager;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PrintManager manager = PrintManager.getInstance();
            if (Files.exists(Path.of(manager.getFilePath()))) {
                try {
                    manager.load();
                } catch (IOException e) {
                    System.err.println("Gespeicherte Aufträge konnten nicht geladen werden: " + e.getMessage());
                }
            }
            new MainWindow().setVisible(true);
        });
    }
}
