package Indexer;

import IR.DocumentInfo;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentIndexer implements Runnable{
    private static volatile DocumentIndexer mInstance;
    private static ConcurrentLinkedQueue<ConcurrentHashMap> docsHashMapsQ;
    public static volatile boolean stopThreads = false;
    public static AtomicInteger numOfFile;

    private DocumentIndexer() {
        docsHashMapsQ = new ConcurrentLinkedQueue<>();
        numOfFile = new AtomicInteger(0);
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
        return docsHashMapsQ.add(newDocHashMap);
    }

    private ConcurrentHashMap deQDocMaps()
    {
        return docsHashMapsQ.poll();
    }

    @Override
    public void run() {
        while (!stopThreads || !docsHashMapsQ.isEmpty())
        {
            writeDocsToDisk();
        }
    }

    private void writeDocsToDisk()
    {
        ConcurrentHashMap<String,DocumentInfo> newDocs = deQDocMaps();
        try {
            String pathToTempFolder = "./docsTempDir/";

            if(!Paths.get(pathToTempFolder).toFile().exists())
            {
                Files.createDirectories(Paths.get(pathToTempFolder));
            }
            FileOutputStream fileOut = new FileOutputStream(pathToTempFolder + numOfFile.getAndIncrement());
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(newDocs);
            objectOut.flush();
            objectOut.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
