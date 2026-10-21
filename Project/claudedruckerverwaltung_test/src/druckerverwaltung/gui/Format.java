package druckerverwaltung.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Kleine Formatierungshilfen für die Anzeige. */
final class Format {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private Format() {
    }

    static String duration(double seconds) {
        if (seconds < 0) {
            return "–";
        }
        long total = Math.round(seconds);
        return String.format("%d:%02d min", total / 60, total % 60);
    }

    static String dateTime(LocalDateTime time) {
        return time == null ? "" : DATE_TIME.format(time);
    }
}
