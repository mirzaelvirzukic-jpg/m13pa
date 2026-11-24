// Printer.java
// D. Singletary
// 11/17/24
// printer simulation
// Mirza Zukic
// 11/23/26
// Synchronize queue access and printing, and run three shared printer workers.

package edu.fscj.cop3330c.printsim;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class IdleState implements PrinterState {
    @Override
    public void processQueue(Printer printer) {
        if (printer.hasPendingJobs()) {
            System.out.println("Printer is transitioning to Printing state...");
            printer.setState(new PrintingState());
            printer.getState().processQueue(printer); // Delegate to PrintingState
        } else {
            System.out.println("Printer is idle. No jobs in the queue.");
        }
    }
}

class PrintingState implements PrinterState {
    @Override
    public void processQueue(Printer printer) {
        if (printer.hasPendingJobs()) {
            PrintJob job = printer.pollJob();
            System.out.println("Submitting Job #" + job.getJobNumber());
            printer.print(job);

            // Check if more jobs are pending
            if (!printer.hasPendingJobs()) {
                System.out.println(
                        "All jobs are printed. Transitioning to Idle state...");
                printer.setState(new IdleState());
            }
        } else {
            System.out.println(
                    "No jobs to print. Transitioning to Idle state...");
            printer.setState(new IdleState());
        }
    }
}

// Printer Class
public class Printer {
    private final Queue<PrintJob> printQueue;
    private final Lock lock;
    // Serializes state transitions and printing while submissions can still enqueue.
    private final Lock processingLock;
    private int jobCounter;
    private PrinterState state;

    public Printer() {
        lock = new ReentrantLock();
        processingLock = new ReentrantLock();
        printQueue = new LinkedList<>();
        state = new IdleState(); // Start in IdleState
    }

    public void setState(PrinterState state) {
        processingLock.lock();
        try {
            this.state = state;
        } finally {
            processingLock.unlock();
        }
    }

    public PrinterState getState() {
        processingLock.lock();
        try {
            return state;
        } finally {
            processingLock.unlock();
        }
    }

    public boolean hasPendingJobs() {
        lock.lock();
        try {
            return !printQueue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public PrintJob pollJob() {
        lock.lock();
        try {
            return printQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    public void addDocument(String document) {
        lock.lock();
        try {
            PrintJob job = new PrintJob(document, ++jobCounter);
            printQueue.offer(job);
            System.out.println("Document added to the queue: " +
                    document + " (Job #" + job.getJobNumber() + ")");
        } finally {
            lock.unlock();
        }
    }

    public void print(PrintJob job) {
        processingLock.lock();
        try {
            System.out.println("Printing Job #" + job.getJobNumber() +
                    ": " + job.getDocumentName());
            try {
                Thread.sleep(1000); // Simulate time taken to print
            } catch (InterruptedException e) {
                System.out.println("Printing was interrupted.");
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("Printing completed for Job #" + job.getJobNumber());
        } finally {
            processingLock.unlock();
        }
    }

    public void processQueue() {
        processingLock.lock();
        try {
            state.processQueue(this);
        } finally {
            processingLock.unlock();
        }
    }

    public static void main(String[] args) {
        final int MAX_IDLE = 5; // seconds
        Printer printer = new Printer();
        DocumentRepository documentRepository = new DocumentRepository();
        Thread[] workers = new Thread[3];

        for (int i = 0; i < workers.length; i++) {
            PrinterWorker worker = new PrinterWorker(printer, documentRepository, MAX_IDLE);
            workers[i] = new Thread(worker, "PrinterWorker-" + (i + 1));
            workers[i].start();
        }

        try {
            for (Thread worker : workers) {
                worker.join();
            }
            System.out.println("All three printer workers finished.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted while waiting for worker threads.");
        }
    }
}
