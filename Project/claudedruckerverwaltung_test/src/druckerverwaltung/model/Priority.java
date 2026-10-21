package druckerverwaltung.model;

/** Priorität eines Druckauftrags. Höhere Priorität wird zuerst gedruckt. */
public enum Priority {
    LOW("Niedrig"),
    NORMAL("Normal"),
    HIGH("Hoch"),
    URGENT("Dringend");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
