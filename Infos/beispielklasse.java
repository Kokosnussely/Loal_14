import java.util.ArrayList;
import java.util.List;

public static class PrintManager {

    private static PrintManager instance;

    private final List<String> jobs = new ArrayList<>();

    private PrintManager() {
    }

    public static synchronized PrintManager getInstance() {
        if (instance == null) {
            instance = new PrintManager();
        }
        return instance;
    }

    public void addJob(String jobName) {
        jobs.add(jobName);
        System.out.println("Added: " + jobName);
    }

    public void printNext() {
        if (jobs.isEmpty()) {
            System.out.println("All done");
            return;
        }
        String job = jobs.remove(0);
        System.out.println("Printing: " + job);
    }

    public int getJobCount() {
        return jobs.size();
    }
}


public static void main(String[] args) {
    PrintManager manager = PrintManager.getInstance();

    manager.addJob("Homework.pdf");
    manager.addJob("Photo.png");
    manager.addJob("Report.docx");
    manager.printNext();
}
