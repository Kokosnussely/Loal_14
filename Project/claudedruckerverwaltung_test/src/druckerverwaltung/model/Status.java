package druckerverwaltung.model;

/** Status eines Druckauftrags. */
public enum Status {
    WAITING("Wartend"),
    IN_PROGRESS("Wird gedruckt"),
    DONE("Fertig"),
    CANCELLED("Abgebrochen");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    /** true, solange der Auftrag noch nicht abgeschlossen ist. */
    public boolean isActive() {
        return this == WAITING || this == IN_PROGRESS;
    }

    @Override
    public String toString() {
        return label;
    }
}
