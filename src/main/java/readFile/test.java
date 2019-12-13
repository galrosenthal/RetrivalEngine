package readFile;

import Indexer.Indexer;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class test {

    private static final int MAX_NUMBER_OF_THREADS = 1;

    public static void main(String[] args) {
//        String corpusPath = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpusTest2";
//        String path = "C:\\Users\\Gal\\Documents\\corpusCopy";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\corpus";
        String corpusPath = "C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\halfCorpus";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\qurtrCorpus";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\10files";
//        String corpusPath = "C:\\Users\\Gal\\Documents\\1files";
//        String postfilePath = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval";
//        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpusTest";














        try
        {
            FileUtils.cleanDirectory(new File("./postingFiles/"));
            FileUtils.cleanDirectory(new File("./dicTemp/"));
        }
        catch (Exception e)
        {
            System.out.println("Could not clean Dirs");
        }

        Indexer myIndexer = Indexer.getInstance();


//        Indexer.getInstance().setPathToPostFiles(postfilePath);
        Thread[] IndexerThreads = new Thread[MAX_NUMBER_OF_THREADS];

        int indexerIndex = 0;
//        for (Thread t :
//                IndexerThreads) {
//            t = new Thread(Indexer.getInstance());
//            t.setName("Indexer " + indexerIndex++);
//            System.out.println(t.getName() + " has started...");
//            t.start();
//        }



        for (int i = 0; i < IndexerThreads.length; i++) {
            IndexerThreads[i] = new Thread(myIndexer);
            IndexerThreads[i].setName("Indexer " + indexerIndex++);
            System.out.println(IndexerThreads[i].getName() + " has started...");
            IndexerThreads[i].start();
        }



        ReadFile f = new ReadFile();
        File corpus = new File(corpusPath);
        long startTime,endTime;
        startTime = System.nanoTime();

        f.readCorpus(corpus);
        //f.runParsers();

        f.stopThreads();

//        for (int i = 0; i < IndexerThreads.length; i++) {
//            System.out.println(IndexerThreads[i].getName() + " has started...");
//            IndexerThreads[i].start();
//        }
//            IndexerThreads[i].start();

//        IndexerThreads[0].stop();


//        long sortStart = System.nanoTime();
//        myIndexer.sortDocListPerTerm();
//        long sortEnd = System.nanoTime();
//        System.out.println("Sorting " + myIndexer.hundredKtermsMap.size() + " Took " + (sortEnd - sortStart)/1000000000 + " Seconds");


//        corpusParsingIndexeingThreads.shutdownNow();
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

        endTime = System.nanoTime();
        System.out.println("There are "+ f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
    }
}
