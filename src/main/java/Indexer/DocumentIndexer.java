package Indexer;

import IR.DocumentInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private ConcurrentHashMap<String, DocumentInfo> dicOfDocs;
    private int countMergedDocument = 0;
    private final String docDelim = "#";
    //TODO: save this parameter with the Dictionary somehow
    // also save the num of docs in the dictionary
    private double avgLengthOfDoc = 0;
    private String pathToPostFolder = "./postingFiles/docTempDir/";

    public DocumentInfo getDocumentInfoOfDoc(String docID)
    {
        if(dicOfDocs.containsKey(docID))
        {
            return dicOfDocs.get(docID);
        }
        return null;
    }

    public void setPathToPostFolder(String pathToPostFolder) {
        this.pathToPostFolder = pathToPostFolder + "/docTempDir/";
    }

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

    public double getAvgLengthOfDoc() throws Exception{
        if(dicOfDocs == null || dicOfDocs.keySet().size() == 0)
        {
            throw new Exception("Could not find Docs Dictionary");
        }

        if(avgLengthOfDoc == 0)
        {
            calculateAvgLengthOfDocument();
        }
        return avgLengthOfDoc;

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
        saveDocParamsToDisk();
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
//            ConcurrentHashMap<String,String> replacedMap = replaceDocInfoToStringMap(dqdHshMap);
            docDequeuerSemaphore.acquireUninterruptibly();
            dicOfDocs.putAll(dqdHshMap);
//            calculateAvgLengthOfDocumentAndInsertToDicOfDocs(dqdHshMap);
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
            docData.append(specificDocInfo.getNumUniqueTerms()).append(docDelim);
            docData.append(specificDocInfo.getDocDate()).append(docDelim);
            docData.append(specificDocInfo.getMaxTfOfTerm());


            // Insert the data to the HashMap
            onlyStringDocData.put(docId,docData.toString());
        }

        return onlyStringDocData;
    }

//    /**
//     * Calculating the avg of doc length in all corpus docs
//     * @param dqdMap
//     */
//    private void calculateAvgLengthOfDocumentAndInsertToDicOfDocs(ConcurrentHashMap<String, DocumentInfo> dqdMap)
//    {
//        for(String docId: dqdMap.keySet())
//        {
//            avgLengthOfDoc = (dqdMap.get(docId).getNumUniqueTerms() + dicOfDocs.keySet().size()*avgLengthOfDoc) / (dicOfDocs.keySet().size()+1);
//            dicOfDocs.put(docId,dqdMap.get(docId));
//        }
//    }
    /**
     * Calculating the avg of doc length in all corpus docs
     */
    private void calculateAvgLengthOfDocument()
    {
        if(dicOfDocs == null || dicOfDocs.size() == 0)
        {
            return;
        }
        int sumOfLengths = 0;
        for(String docId: dicOfDocs.keySet())
        {
//            avgLengthOfDoc = (dicOfDocs.get(docId).getNumUniqueTerms() + dicOfDocs.keySet().size()*avgLengthOfDoc) / (dicOfDocs.keySet().size()+1);
//            sumOfLengths += dicOfDocs.get(docId).getNumUniqueTerms();
            sumOfLengths += dicOfDocs.get(docId).getDocLength();
        }
        avgLengthOfDoc = sumOfLengths / dicOfDocs.keySet().size();
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
            String pathToTempFolder = pathToPostFolder;

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
     * Save num of total Doc that were indexed and the avg length of a doc in the corpus to the disk.
     */
    private void saveDocParamsToDisk()
    {
        try {
            String pathToTempFolder = pathToPostFolder + "/";
            calculateAvgLengthOfDocument();

            if (!Paths.get(pathToTempFolder).toFile().exists()) {
                Files.createDirectories(Paths.get(pathToTempFolder));
            }

            FileOutputStream paramsFile = new FileOutputStream(pathToTempFolder + "params.txt");
            StringBuilder params = new StringBuilder();
            params.append(dicOfDocs.size()).append("\n");;
            params.append(avgLengthOfDoc).append("\n");;

            paramsFile.write(params.toString().getBytes());
            paramsFile.flush();
            paramsFile.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Load the params of all documents from the disk and the Dictionary,
     * such as:
     *    num of all doc in the corpus
     *    avg length of a doc in the corpus
     */
    public boolean loadDictionaryFromDisk()
    {
        try
        {
            String pathToTempFolder = pathToPostFolder+"/";
            if(dicOfDocs != null && dicOfDocs.keySet().size() != 0)
            {
                return false;
            }
            if (!Paths.get(pathToTempFolder).toFile().exists()) {
                throw new Exception("Could not find the Directory");
            }
            Path pathTofolderOfDocs = Paths.get(pathToTempFolder);
            File folderOfDocs = pathTofolderOfDocs.toFile();
            int numOfFilesInFolder = 0;
            if(!folderOfDocs.isDirectory())
            {
                throw new Exception("Something went wrong supposed to have directory");
            }
            numOfFilesInFolder = folderOfDocs.listFiles().length;
            numOfFile.set(0);
            dicOfDocs = new ConcurrentHashMap<>();
            while(numOfFile.get()<numOfFilesInFolder-1)
            {
                ObjectInputStream getDicFromDisk = new ObjectInputStream(new FileInputStream(pathToTempFolder + numOfFile.getAndIncrement()));
                Object dic = getDicFromDisk.readObject();
                ConcurrentHashMap dicFromDisk = (ConcurrentHashMap<String,DocumentInfo>)dic;
                dicOfDocs.putAll(dicFromDisk);
                getDicFromDisk.close();
            }

            File paramsReader = new File(pathToTempFolder+"params.txt");
            BufferedReader bufReader = new BufferedReader(new FileReader(paramsReader));
            String line = "";
            ArrayList<String> allLinesInFile = new ArrayList<>();
            while((line = bufReader.readLine()) != null)
            {
                allLinesInFile.add(line);
            }

            int dicOfDocSize = Integer.parseInt(allLinesInFile.get(0));
            avgLengthOfDoc = Double.parseDouble(allLinesInFile.get(1));

            if(dicOfDocSize != dicOfDocs.size())
            {
                throw new Exception("Something went wrong reading the Document dictionary, not as same size as saved");
            }
            return true;
//            paramsReader.delete();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
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

    /**
     * @return the size of the dictionary
     * @throws Exception
     */
    public int getSizeOfDictionary() throws Exception{
        if(dicOfDocs == null || dicOfDocs.size() == 0)
        {
            throw new Exception("Dictionary could not be found");
        }

        return dicOfDocs.size();
    }

    /**
     * Gets the Length of the Document <u>docId</u>
     * @param docId The id of the doc to get its length
     * @return the length of the document
     */
    public int getLengthOfDoc(String docId)
    {
        if(dicOfDocs == null || dicOfDocs.size() == 0) {
            return 0;
        }
        return dicOfDocs.get(docId).getNumUniqueTerms();
    }
}
