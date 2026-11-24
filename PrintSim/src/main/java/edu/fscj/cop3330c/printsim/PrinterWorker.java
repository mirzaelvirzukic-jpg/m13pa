// PrinterWorker.java
// D. Singletary
// 11/17/24
// printer simulation
// Mirza Zukic
// 11/23/26
// Pause when the repository is empty and honor thread interruption.

package edu.fscj.cop3330c.printsim;

public class PrinterWorker implements Runnable {
    private final Printer printer;
    private final DocumentRepository documentRepository;
    private final int maxIdleTime; // seconds

    public PrinterWorker(Printer printer,
                         DocumentRepository documentRepository,
                         int maxIdleTime) {
        this.printer = printer;
        this.documentRepository = documentRepository;
        this.maxIdleTime = maxIdleTime;
    }

    @Override
    public void run() {
        long lastAddedTime = System.currentTimeMillis();

        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Thread ID: " + Thread.currentThread().getId() +
                    " Obtaining the next documents...");

            String document =
                    documentRepository.getNextDocument();

            if (document != null) {
                lastAddedTime = System.currentTimeMillis(); // Reset the last added time
                printer.addDocument(document);
            } else {
                System.out.println("No more documents in the repository.");
            }

            System.out.println("Checking the queue...");
            printer.processQueue(); // Delegate behavior to the current state

            // Exit condition: more than maxIdleTime seconds with no new documents added
            if (System.currentTimeMillis() - lastAddedTime > maxIdleTime * 1000) {
                System.out.println("Idle time exceeded. Worker thread exiting...");
                break;
            }

            if (document == null) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
