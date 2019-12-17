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

public class DocumentIndexer implements Runnable{
    private static final int MAX_DOCS_TO_INDEX = Integer.MAX_VALUE;
    private static volatile DocumentIndexer mInstance;
    private static ConcurrentLinkedQueue<ConcurrentHashMap> docsHashMapsQ;
    public static volatile boolean stopThreads = false;
    public static AtomicInteger numOfFile;
    public static Semaphore docDequeuerSemaphore;
    private ConcurrentHashMap<String, String> dicOfDocs;
    private int countMergedTerms = 0;
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


    public static boolean enQnewDocs(ConcurrentHashMap<String, DocumentInfo> newDocHashMap)
    {
        docDequeuerSemaphore.acquireUninterruptibly();
        boolean isAdded = docsHashMapsQ.add(newDocHashMap);
        docDequeuerSemaphore.release();
        return isAdded;

    }

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
        writeDocsToDisk();
    }

    private void generateDicOfDocs() {
        while (!isQEmpty()) {
//            System.out.println("There are " + parsedWordsQueue.size() + " Maps left in the Q");
            ConcurrentHashMap<String, DocumentInfo> dqdHshMap = deQDocMaps();

            if (dqdHshMap == null || dqdHshMap.size() == 0) {
                continue;
            }
            long startTime, endTime;


//            System.out.println("Merging "+dqdHshMap.size());
            int mapSizeBeforeMerge = dicOfDocs.size();
            startTime = System.nanoTime();
            ConcurrentHashMap<String,String> replacedMap = replaceDocInfoToStringMap(dqdHshMap);
            docDequeuerSemaphore.acquireUninterruptibly();
//            mergeHashMapIntoDocMaps(dqdHshMap, dicOfDocs);
            dicOfDocs.putAll(replacedMap);
            docDequeuerSemaphore.release();
            endTime = System.nanoTime();
            int mapSizeAfterMerge = dicOfDocs.size();
            countMergedTerms += (mapSizeAfterMerge - mapSizeBeforeMerge);
//            System.out.println("Merging took "+(endTime - startTime)/1000000000 + " seconds");

            if (countMergedTerms >= MAX_DOCS_TO_INDEX) {
//                System.out.println("Sorting "+hundredKtermsMap.size());
//                startTime = System.nanoTime();
//                sortDocListPerTerm();
                writeDocsToDisk();
//                endTime = System.nanoTime();
                countMergedTerms = 0;
//                System.out.println("Sorting took "+(endTime - startTime)/1000000000 + " seconds");
            }

        }
    }

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
            docData.append(specificDocInfo.getNumUniqeTerms()).append(docDelim);
            docData.append(specificDocInfo.getDocDate());

            // Insert the data to the HashMap
            onlyStringDocData.put(docId,docData.toString());
        }

        return onlyStringDocData;
    }

//    /**
//     * Gets 2 HashMaps
//     * and merges equal term and theris values
//     *
//     * @param hashMapToMergeFrom - the HashMap its value you want to merge
//     * @param hashMapToMergeTo   - the HashMap you want to merge the terms into it
//     */
//    private void mergeHashMapIntoDocMaps(ConcurrentHashMap<String,DocumentInfo> hashMapToMergeFrom, ConcurrentHashMap<String,DocumentInfo> hashMapToMergeTo) {
//        hashMapToMergeFrom.forEach(
//                (key, value) -> hashMapToMergeTo.merge(key, value, (v1, v2) -> v1 + ";" + v2)
//        );
//    }

    private boolean isQEmpty() {
        docDequeuerSemaphore.acquireUninterruptibly();
        boolean isEmpty = docsHashMapsQ.isEmpty();
        docDequeuerSemaphore.release();
        return isEmpty;
    }

    private void writeDocsToDisk()
    {
//        ConcurrentHashMap<String,DocumentInfo> newDocs = deQDocMaps();
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

    public void resetDocumentIndexer(){
        docsHashMapsQ = new ConcurrentLinkedQueue<>();
        numOfFile = new AtomicInteger(0);
        docDequeuerSemaphore = new Semaphore(1);
        dicOfDocs = new ConcurrentHashMap<>();
        stopThreads = false;
    }
}
