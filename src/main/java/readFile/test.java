package readFile;

import IR.Document;
import Indexer.DocumentIndexer;
import Indexer.Indexer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class test {

    private static final int MAX_NUMBER_OF_THREADS = 2;

    public static void main(String[] args) {
//        String corpusPath = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpusTest";
//        String path = "C:\\Users\\Gal\\Documents\\corpusCopy";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\corpus";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\halfCorpus";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\qurtrCorpus";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\10files";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\1files";
//        String postfilePath = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval";
//        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpusTest";


//        String corpusPath = "d:\\documents\\users\\rosengal\\Documents\\corpus";
//        String corpusPath = "d:\\documents\\users\\rosengal\\Documents\\halfCorpus";
        String corpusPath = "d:\\documents\\users\\rosengal\\Documents\\qurtrCorpus";
//        String corpusPath = "d:\\documents\\users\\rosengal\\Documents\\10files";
//        String corpusPath = "d:\\documents\\users\\rosengal\\Documents\\2files";
//        String corpusPath = "d:\\documents\\users\\rosengal\\Documents\\1files";

//        readDocsHashMapToDisk();
        try
        {
            FileUtils.cleanDirectory(new File("./postingFiles/"));
            FileUtils.cleanDirectory(new File("./dicTemp/"));
            FileUtils.cleanDirectory(new File("./docsTempDir/"));
        }
        catch (Exception e)
        {
            System.out.println("Could not clean Dirs");
        }
        Indexer myIndexer = Indexer.getInstance();
        DocumentIndexer docIndexer = DocumentIndexer.getInstance();

//        myIndexer.createCorpusDictionary();

//        Indexer.getInstance().setPathToPostFiles(postfilePath);
        Thread[] IndexerThreads = new Thread[MAX_NUMBER_OF_THREADS];

        int indexerIndex = 0;

        IndexerThreads[0] = new Thread(myIndexer);
        IndexerThreads[0].setName("Term Indexer");
        IndexerThreads[1] = new Thread(docIndexer);
        IndexerThreads[1].setName("Doc Indexer");


        for (int i = 0; i < IndexerThreads.length; i++) {
//            IndexerThreads[i] = new Thread(myIndexer);
//            IndexerThreads[i].setName("Indexer " + indexerIndex++);
            System.out.println(IndexerThreads[i].getName() + " has started...");
            IndexerThreads[i].start();
        }


        ReadFile f = new ReadFile(false);
        File corpus = new File(corpusPath);
        long startTime,endTime;
        startTime = System.nanoTime();

        f.readCorpus(corpus);
        //f.runParsers();

        f.stopThreads();

        try{
            for (int i = 0; i < IndexerThreads.length; i++) {

                IndexerThreads[i].join();
                System.out.println(IndexerThreads[i].getName() + " has stopped...");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        myIndexer.createCorpusDictionary();

        myIndexer.saveCorpusDictionary();
//        writeDocsHashMapToDisk(MainParse.allDocs);
//        readDocsHashMapToDisk();
        System.out.println("Corpus Size = " + myIndexer.corpusSize());


        endTime = System.nanoTime();
        System.out.println("There are "+ f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
    }

    private static void readDocsHashMapToDisk() {
        try {
            String pathToTempFolder = "./docsTempDir/";
            int numOfFile = 0;
            if(!Paths.get(pathToTempFolder).toFile().exists())
            {
                Files.createDirectories(Paths.get(pathToTempFolder));
            }
            FileInputStream filein = new FileInputStream(pathToTempFolder + 0);
            ObjectInputStream objectOut = new ObjectInputStream(filein);
            Object a = objectOut.readObject();
            objectOut.close();
            filein.close();

            ConcurrentHashMap<String, Document> myMap = (ConcurrentHashMap)a;

//            filein = new FileInputStream(pathToTempFolder + 1);
//            objectOut = new ObjectInputStream(filein);
//            a = objectOut.readObject();
//            objectOut.close();
//            filein.close();
//
//
//            ConcurrentHashMap<String, Document> myMap2 = (ConcurrentHashMap)a;

            System.out.println("Wtfffff");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    private static void writeDocsHashMapToDisk(ConcurrentHashMap<String, DocumentInfo> allDocs) {
//        try {
//            String pathToTempFolder = "./docsTempDir/";
//            int numOfFile = 0;
//            if(!Paths.get(pathToTempFolder).toFile().exists())
//            {
//                Files.createDirectories(Paths.get(pathToTempFolder));
//            }
//            FileOutputStream fileOut = new FileOutputStream(pathToTempFolder + numOfFile++);
//            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
//            objectOut.writeObject(allDocs);
//            objectOut.flush();
//            objectOut.close();
//            fileOut.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
