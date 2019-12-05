package readFile;

import Indexer.Indexer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class test {

    private static final int MAX_NUMBER_OF_THREADS = 4;

    public static void main(String[] args) {
        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpus";
        String test = "-1010.56";
        String corpusPath = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpus";
        String postfilePath = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpusTest2";
//        test = test.substring(0,test.indexOf("."));
        test = test.replaceAll(",","");
        Double testValue = Double.valueOf(test);

    /*
        Indexer.getInstance().setPathToPostFiles(postfilePath);
        ExecutorService indexerThreads = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        indexerThreads.execute(Indexer.getInstance());
        indexerThreads.execute(Indexer.getInstance());
        indexerThreads.execute(Indexer.getInstance());
        indexerThreads.execute(Indexer.getInstance());
        indexerThreads.execute(Indexer.getInstance());
        indexerThreads.execute(Indexer.getInstance());
        indexerThreads.execute(Indexer.getInstance());
        indexerThreads.execute(Indexer.getInstance());

*/
        ReadFile f = new ReadFile();
        File corpus = new File(corpusPath);
        long startTime,endTime;
        startTime = System.nanoTime();
        f.readCorpus(corpus);
        endTime = System.nanoTime();
        //HashMap<String,Integer> testNumInAllCorpus = f.prsNums.getNumbersInText();

        Indexer.stopThreads = true;

        System.out.println("There are "+f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
    }
}
