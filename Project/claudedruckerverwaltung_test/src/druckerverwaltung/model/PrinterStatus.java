package druckerverwaltung.model;

/** Zustand eines Druckers. */
public enum PrinterStatus {
    IDLE("Bereit"),
    BUSY("Druckt"),
    OFFLINE("Offline"),
    ERROR("Fehler");

    private final String label;

    PrinterStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
