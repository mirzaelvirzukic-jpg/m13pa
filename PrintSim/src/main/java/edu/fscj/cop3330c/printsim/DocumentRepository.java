// DocumentRepository.java
// D. Singletary
// 11/17/24
// manage documents for printer simulation
// Mirza Zukic
// 11/23/26
// Protect document retrieval and availability checks with a ReentrantLock.

package edu.fscj.cop3330c.printsim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DocumentRepository {
    private final List<String> documentList;
    private final Lock lock;
    private int documentIndex;

    public DocumentRepository() {
        this.lock = new ReentrantLock();
        this.documentList = new ArrayList<>();
        this.documentIndex = 0;
        initializeDocuments();
    }

    // Initialize the document list
    private void initializeDocuments() {
        for (int i = 1; i <= 50; i++) {
            documentList.add("Document_" +
                    String.format("%03d", i) + ".docx");
        }
    }

    public String getNextDocument() {
        lock.lock();
        try {
            if (documentIndex >= documentList.size()) {
                return null;
            }
            return documentList.get(documentIndex++);
        } finally {
            lock.unlock();
        }
    }

    public boolean hasMoreDocuments() {
        lock.lock();
        try {
            return documentIndex < documentList.size();
        } finally {
            lock.unlock();
        }
    }
}
