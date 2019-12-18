package Indexer;

import IR.DocumentInfo;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Indexer.DocumentIndexer Class is Singleton class thats do the indexing of the Corpus Documents
 * it has 9 variables:
 * MAX_DOCS_TO_INDEX - A Final int used to set the maximum docs to keep in the RAM before saving to disk
 * mInstance - The Instance of the Singleton class
 * docsHashMapsQ - The Queue of the Documents to Index
 * stopThreads - a boolean variable to indicate whether the thread should stop waiting for new docs or not
 * numOfFile - Atomic Integer that gives a number for each file written to the disk (should be equals to (number of documents in the corpus/MAX_DOCS_TO_INDEX))
 * docDequeuerSemaphore - The Semaphore that holds the queue from dequeue/enqueue while other thread is doing so
 * dicOfDocs - The Dictionary of the Document Indexed
 * countMergedDocument - Counts how many documents were parsed in each iteration must be less than <I>MAX_DOCS_TO_INDEX</I>
 * docDelim - The Delimiter that separates the data inside the document info
 */
public class DocumentIndexer implements Runnable{
    private static final int MAX_DOCS_TO_INDEX = Integer.MAX_VALUE;
    private static volatile DocumentIndexer mInstance;
    private static ConcurrentLinkedQueue<ConcurrentHashMap> docsHashMapsQ;
    public static volatile boolean stopThreads = false;
    public static AtomicInteger numOfFile;
    public static Semaphore docDequeuerSemaphore;
    private ConcurrentHashMap<String, String> dicOfDocs;
    private int countMergedDocument = 0;
    private final String docDelim = "#";

    private DocumentIndexer() {
        docsHashMapsQ = new ConcurrentLinkedQueue<>();
//        docsHashMapsQ = new LinkedBlockingDeque<>();
        numOfFile = new AtomicInteger(0);
        docDequeuerSemaphore = new Semaphore(1);
        dicOfDocs = new ConcurrentHashMap<>();
    }

    public static DocumentIndexer getInstance() {
        if (mInstance == null) {
            synchronized (DocumentIndexer.class) {
                if (mInstance == null) {
                    mInstance = new DocumentIndexer();
                }
            }
        }
        return mInstance;
    }


    /**
     * Enqueues a new HashMap of docs to the Queue
     * @param newDocHashMap - the hashmap to enqueue
     * @return true if enqueue succeed
     */
    public static boolean enQnewDocs(ConcurrentHashMap<String, DocumentInfo> newDocHashMap)
    {
        docDequeuerSemaphore.acquireUninterruptibly();
        boolean isAdded = docsHashMapsQ.add(newDocHashMap);
        docDequeuerSemaphore.release();
        return isAdded;

    }

    /**
     * Dequeue a new HashMap of docs from the Queue
     * @return the hashMap Dequeued
     */
    private ConcurrentHashMap deQDocMaps()
    {
        docDequeuerSemaphore.acquireUninterruptibly();
        ConcurrentHashMap deqdHashMap = docsHashMapsQ.poll();
        docDequeuerSemaphore.release();
        return deqdHashMap;
    }

    @Override
    public void run() {
        while (!stopThreads || !docsHashMapsQ.isEmpty())
        {
//            writeDocsToDisk();
            generateDicOfDocs();
        }
        // if the were still documents that were not index after the Q is empty and the thread is stopped
        // write them to the disk
        writeDocsToDisk();
    }

    /**
     * Working on the HashMaps in the Q and creates the Indexed Dictionary of the Docs.
     */
    private void generateDicOfDocs() {
        while (!isQEmpty()) {
            ConcurrentHashMap<String, DocumentInfo> dqdHshMap = deQDocMaps();

            if (dqdHshMap == null || dqdHshMap.size() == 0) {
                continue;
            }


            int mapSizeBeforeMerge = dicOfDocs.size();
            ConcurrentHashMap<String,String> replacedMap = replaceDocInfoToStringMap(dqdHshMap);
            docDequeuerSemaphore.acquireUninterruptibly();
            dicOfDocs.putAll(replacedMap);
            docDequeuerSemaphore.release();
            int mapSizeAfterMerge = dicOfDocs.size();
            countMergedDocument += (mapSizeAfterMerge - mapSizeBeforeMerge);

            if (countMergedDocument >= MAX_DOCS_TO_INDEX) {
                writeDocsToDisk();
                countMergedDocument = 0;
            }

        }
    }

    /**
     * Gets a HashMap of <String(DocID),DocumentInfo> and generates the same HashMap but of <String(DocID),String(DocumentInfo)>
     * @param dqdHshMap - The HashMap to change.
     * @return The Changed HashMap
     */
    private ConcurrentHashMap<String, String> replaceDocInfoToStringMap(ConcurrentHashMap<String, DocumentInfo> dqdHshMap) {
        StringBuilder docData;
        ConcurrentHashMap<String,String> onlyStringDocData = new ConcurrentHashMap<>();
        for (String docId :
                dqdHshMap.keySet()) {
            docData = new StringBuilder();
            DocumentInfo specificDocInfo = dqdHshMap.get(docId);
            //Append all Document Info to one String
            docData.append(specificDocInfo.getDocNo()).append(docDelim);
            docData.append(specificDocInfo.getMaxTfTerm()).append(docDelim);
            docData.append(specificDocInfo.getNamUniqueTerms()).append(docDelim);
            docData.append(specificDocInfo.getDocDate());

            // Insert the data to the HashMap
            onlyStringDocData.put(docId,docData.toString());
        }

        return onlyStringDocData;
    }


    /**
     * @return true if the Q is empty
     */
    private boolean isQEmpty() {
        docDequeuerSemaphore.acquireUninterruptibly();
        boolean isEmpty = docsHashMapsQ.isEmpty();
        docDequeuerSemaphore.release();
        return isEmpty;
    }


    /**
     * Writes the currently generated Document Dictionary to the Disk as Object in pathToTempFolder/numOfFile
     */
    private void writeDocsToDisk()
    {
        try {
            String pathToTempFolder = "./docsTempDir/";

            if(!Paths.get(pathToTempFolder).toFile().exists())
            {
                Files.createDirectories(Paths.get(pathToTempFolder));
            }
            docDequeuerSemaphore.acquireUninterruptibly();
            FileOutputStream fileOut = new FileOutputStream(pathToTempFolder + numOfFile.getAndIncrement());
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(dicOfDocs);
            objectOut.flush();
            objectOut.close();
            fileOut.close();

            dicOfDocs = new ConcurrentHashMap<>();
            docDequeuerSemaphore.release();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the variables of the indexer
     */
    public void resetDocumentIndexer(){
        docsHashMapsQ = new ConcurrentLinkedQueue<>();
        numOfFile = new AtomicInteger(0);
        docDequeuerSemaphore = new Semaphore(1);
        dicOfDocs = new ConcurrentHashMap<>();
        stopThreads = false;
    }
}
