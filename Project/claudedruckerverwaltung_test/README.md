# Druckerverwaltung (Singleton) – Testprojekt

Java-Swing-Anwendung zur Verwaltung von Druckaufträgen, umgesetzt nach
`Infos/DokumentenverwaltungInfos` und den Klassen aus `Infos/FreieNotizenFragen`.

## Starten

Benötigt Java 17 oder neuer (getestet mit Java 21).

```sh
javac -d out $(find src -name '*.java')
java -cp out druckerverwaltung.Main
```

In IntelliJ: Ordner öffnen, `src` als Sources Root markieren, `druckerverwaltung.Main` starten.

## Wo ist das Singleton – und warum genau dort?

`druckerverwaltung.manager.PrintManager` ist das **einzige** Singleton:

- `private PrintManager()` – von außen ist kein `new PrintManager()` möglich
- `private static PrintManager instance` – hält die einzige Instanz
- `public static synchronized PrintManager getInstance()` – erzeugt die Instanz beim ersten Aufruf

Die Ressource, die nur einmal existieren darf, ist die **zentrale Auftragsliste samt
Druckerwarteschlangen**. Gäbe es zwei Verwaltungen, würden Fenster unterschiedliche
Auftragslisten sehen und zwei Verwaltungen könnten denselben Drucker gleichzeitig befüllen.

`PrintJob`, `Printer`, `HistoryEntry` und die Fenster gibt es dagegen mehrfach – sie sind
deshalb bewusst **keine** Singletons. Alle Fenster (`MainWindow`, `JobDialog`, `StatusPanel`,
`JobTableModel`, `HistoryWindow`) holen sich die Verwaltung über `PrintManager.getInstance()`
und melden sich per `addChangeListener` an, damit sie sich bei Änderungen aktualisieren.

## Aufbau

| Paket | Klassen |
|---|---|
| `druckerverwaltung.model` | `PrintJob`, `Priority`, `Status`, `Printer`, `PrinterStatus`, `HistoryEntry`, `Statistics` |
| `druckerverwaltung.manager` | `PrintManager` «Singleton» |
| `druckerverwaltung.gui` | `MainWindow`, `JobDialog`, `StatusPanel`, `JobTableModel`, `HistoryWindow` |

## Funktionen

Muss-Anforderungen:
- Druckaufträge erstellen, anzeigen, löschen
- Prioritäten verwalten (auch für bereits eingereihte Aufträge)
- Druckstatus anzeigen (pro Auftrag mit Fortschrittsbalken und pro Drucker)
- Aufträge speichern/laden (`druckauftraege.txt`, Tab-getrennt); beim Beenden wird gefragt

Kann-Anforderungen:
- mehrere Drucker (Laserdrucker, Farbdrucker, Plotter) – online/offline schaltbar
- Prioritätswarteschlange pro Drucker (`PriorityQueue`, höchste Priorität zuerst)
- simulierte Druckerwarteschlange: 1 echte Sekunde = 10 simulierte Sekunden
- geschätzte Druckdauer pro Auftrag und Restzeit pro Drucker
- Druckhistorie und Statistiken im Fenster „Historie & Statistik“

„Starten“ schickt einen Auftrag an den Drucker, der ihn am schnellsten fertig drucken kann.
