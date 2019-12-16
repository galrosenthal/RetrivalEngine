package GUI.Model;

import Indexer.Indexer;
import org.apache.commons.io.FileUtils;
import readFile.ReadFile;
import Indexer.DocumentIndexer;

import java.io.File;
import java.util.HashMap;
import java.util.Observable;

public class Model extends Observable implements IModel {
    String corpusPath;
    String postingPath;
    private static final int MAX_NUMBER_OF_THREADS = 2;


    @Override
    public void loadDictionary(boolean withStemm) {
        Indexer myIndexer = Indexer.getInstance();
        myIndexer.loadDictionary(withStemm);
    }

    public void startParse(String corpusPath, String postingPath, boolean withStemm){
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


        ReadFile f = new ReadFile(withStemm);
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

        myIndexer.saveCorpusDictionary(false);
//        writeDocsHashMapToDisk(MainParse.allDocs);
//        readDocsHashMapToDisk();
        System.out.println("Corpus Size = " + myIndexer.corpusSize());


        endTime = System.nanoTime();
        System.out.println("There are "+ f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
        setChanged();
        notifyObservers(1);
    }

    @Override
    public void setReset(String corpusPath,String postingPath) {
        try
        {
            if(corpusPath!=null && postingPath!=null){
                //FileUtils.cleanDirectory(new File(corpusPath));
                FileUtils.cleanDirectory(new File(postingPath));
            }
        }
        catch (Exception e)
        {
            //System.out.println("Could not clean Dirs");
        }
    }

    public HashMap<String,String> getDictionary(){

        Indexer myIndexer = Indexer.getInstance();

        return myIndexer.getCorpusDictionary();
    }


}
