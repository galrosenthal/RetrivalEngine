package GUI.Model;

import Indexer.Indexer;
import org.apache.commons.io.FileUtils;
import readFile.ReadFile;
import Indexer.DocumentIndexer;

import java.io.File;
import java.util.HashMap;
import java.util.Observable;

/**
 * This class runing all the logic of the program, creating threads of parser and indexers and run them,
 * after they he inform the viewmodel that he is finished , also the class load the dictionary adn save the dictionary
 */
public class Model extends Observable implements IModel {
    private static final int MAX_NUMBER_OF_THREADS = 2;
    String alertToShow;

    @Override
    public void loadDictionary(boolean withStemm,String postingPath) {
        setPathToIndexer(postingPath, withStemm);
        Indexer myIndexer = Indexer.getInstance();
        boolean loadSucc = myIndexer.loadDictionary(withStemm);
        if(loadSucc){
            setChanged();
            notifyObservers(3);
        }
    }

    /**
     * Strating the program
     * @param corpusPath the path which the corpus exist
     * @param postingPath the path which the posting will be
     * @param withStemm boolean if the paring with stemming or not
     */
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
        myIndexer.exportToCSV();
//        writeDocsHashMapToDisk(MainParse.allDocs);
//        readDocsHashMapToDisk();
        System.out.println("Corpus Size = " + myIndexer.corpusSize());


        endTime = System.nanoTime();
       alertToShow = "There are "+ f.numOfParsedDocs + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all";


       if(IndexerThreads[0].isAlive()){
           System.out.println("I am alive");
       }

        setChanged();
        notifyObservers(1);
    }

    /**
     * Setting the path to the choosing path from the user
     * @param postingPath
     * @param withStemm
     */
    private void setPathToIndexer(String postingPath, boolean withStemm) {
        if(withStemm){
            Indexer.getInstance().setPathToPostFiles(postingPath + "/postingWithStemm");
        }
        else{
            Indexer.getInstance().setPathToPostFiles(postingPath + "/postingNoStemm");
        }
    }

    /**
     * Implementing reset option in the gui.
     * Deleting all temp files and cleaning the objects by
     * using the resetObject method
     * @param corpusPath
     * @param postingPath
     */
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

        }

        setChanged();
        notifyObservers(2);
    }

    /**
     * Give the hash map of the corpus dictionary
     * @return the orpus dictionary
     */
    public HashMap<String,String> getDictionary(){

        Indexer myIndexer = Indexer.getInstance();

        return myIndexer.getCorpusDictionary();
    }

    /**
     * Gives the controller the message he need to show
     * @return the message
     */
    public String getAlertToShowFinish(){
        return alertToShow;
    }

    /**
     *Calling two function in each class and initialize all the object int the class
     */
    public  void resetObject(){
        Indexer.getInstance().resetIndexer();
        DocumentIndexer.getInstance().resetDocumentIndexer();
    }
}
