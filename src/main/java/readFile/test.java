package readFile;

import Indexer.Indexer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class test {

    private static final int MAX_NUMBER_OF_THREADS = 4;

    public static void main(String[] args) {
//        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpus";
//        String path = "C:\\Users\\Gal\\Documents\\corpusCopy";
        String corpusPath = "C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\corpus";
        String postfilePath = "C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\posts";
//        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpusTest";


        String test = "-1010.56";
//        test = test.substring(0,test.indexOf("."));
        test = test.replaceAll(",","");
        Double testValue = Double.valueOf(test);


        Indexer.getInstance().setPathToPostFiles(postfilePath);
        ThreadPoolExecutor crpsThrds =(ThreadPoolExecutor)Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        ExecutorService corpusParsingIndexeingThreads = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        for (int i = 0; i < MAX_NUMBER_OF_THREADS; i++) {
            corpusParsingIndexeingThreads.execute(Indexer.getInstance());
        }





        ReadFile f = new ReadFile();
        File corpus = new File(corpusPath);
        long startTime,endTime;
        startTime = System.nanoTime();
        f.readCorpus(corpus);
        endTime = System.nanoTime();
        //HashMap<String,Integer> testNumInAllCorpus = f.prsNums.getNumbersInText();

//        Indexer.stopThreads = true;
//        while(!Indexer.stopThreads)
//        {
//
//        }
        try
        {
            if(!corpusParsingIndexeingThreads.awaitTermination(300, TimeUnit.SECONDS))
            {
                System.out.println("Timed out");
                Indexer.stopThreads = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        corpusParsingIndexeingThreads.shutdownNow();

        System.out.println("There are "+f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
    }
}
