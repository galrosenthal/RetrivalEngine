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
    String alertToShow;

    @Override
    public void loadDictionary(boolean withStemm) {
        setPathToIndexer(postingPath, withStemm);
        Indexer myIndexer = Indexer.getInstance();
        myIndexer.loadDictionary(withStemm);
    }

    public void startParse(String corpusPath, String postingPath, boolean withStemm){
        resetObject();
        try
        {
            //FileUtils.cleanDirectory(new File(postingPath));
            FileUtils.cleanDirectory(new File("./dicTemp/"));
            FileUtils.cleanDirectory(new File("./docsTempDir/"));
            if(withStemm)
            {
                FileUtils.cleanDirectory(new File(postingPath + "/postingWithStemm"));
            }
            else
            {
                FileUtils.cleanDirectory(new File(postingPath + "/postingNoStemm"));
            }
        }
        catch (Exception e)
        {
            System.out.println("Could not clean Dirs");
        }

        Indexer myIndexer = Indexer.getInstance();
        DocumentIndexer docIndexer = DocumentIndexer.getInstance();

//        myIndexer.createCorpusDictionary();
        setPathToIndexer(postingPath, withStemm);
        Thread[] IndexerThreads = new Thread[MAX_NUMBER_OF_THREADS];

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

        System.gc();
        myIndexer.createCorpusDictionary();

        myIndexer.removeEntitys();

        myIndexer.saveCorpusDictionary(withStemm);
//        writeDocsHashMapToDisk(MainParse.allDocs);
//        readDocsHashMapToDisk();
        System.out.println("Corpus Size = " + myIndexer.corpusSize());


        endTime = System.nanoTime();
       alertToShow = "There are "+ f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all";


       if(IndexerThreads[0].isAlive()){
           System.out.println("I am alive");
       }

        setChanged();
        notifyObservers(1);
    }

    private void setPathToIndexer(String postingPath, boolean withStemm) {
        if(withStemm){
            Indexer.getInstance().setPathToPostFiles(postingPath + "/postingWithStemm");
        }
        else{
            Indexer.getInstance().setPathToPostFiles(postingPath + "/postingNoStemm");
        }
    }

    @Override
    public void setReset(String corpusPath,String postingPath) {
        try
        {
            if(corpusPath!=null && postingPath!=null){
                FileUtils.cleanDirectory(new File(postingPath));
                FileUtils.cleanDirectory(new File("./dicTemp/"));
                FileUtils.cleanDirectory(new File("./docsTempDir/"));
            }

            resetObject();


        }
        catch (Exception e)
        {
            //System.out.println("Could not clean Dirs");
        }

        setChanged();
        notifyObservers(2);
    }

    public HashMap<String,String> getDictionary(){

        Indexer myIndexer = Indexer.getInstance();

        return myIndexer.getCorpusDictionary();
    }

    public String getAlertToShowFinish(){
        return alertToShow;
    }

    public  void resetObject(){
        Indexer.getInstance().resetIndexer();
        DocumentIndexer.getInstance().resetDocumentIndexer();
    }
}
