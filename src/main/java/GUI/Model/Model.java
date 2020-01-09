package GUI.Model;

import Indexer.Indexer;
import Searcher.Searcher;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import readFile.ReadFile;
import Indexer.DocumentIndexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

/**
 * This class runing all the logic of the program, creating threads of parser and indexers and run them,
 * after they he inform the viewmodel that he is finished , also the class load the dictionary adn save the dictionary
 */
public class Model extends Observable implements IModel {
    private static final int MAX_NUMBER_OF_THREADS = 2;
    String alertToShow;
    int id =0;
    HashMap<String,List<String>> queryResFile;
    List<String> queryRes;

    @Override
    public void loadDictionary(boolean withStemm,String postingPath) {
        setPathToIndexer(postingPath, withStemm);
        Indexer myIndexer = Indexer.getInstance();
        boolean loadSucc = myIndexer.loadDictionary(withStemm);
        DocumentIndexer docIndex = DocumentIndexer.getInstance();
        boolean loadDocSucc = docIndex.loadDictionaryFromDisk();;
        if(loadSucc && loadDocSucc){
            setChanged();
            notifyObservers(3);
        }
        else
        {
            setChanged();
            notifyObservers(4);
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
//            FileUtils.cleanDirectory(new File("./docsTempDir/"));
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
        f.setCorpusPath(corpusPath);
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
       alertToShow = "There are "+ f.numOfParsedDocs + " files in the corpus \n Time: " + (endTime - startTime)/1000000000 + " Seconds\n"+
       "Corpus Size: " + myIndexer.corpusSize();


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
    private void setPathToIndexer(String postingPath, boolean withStemm)
    {
        try {
            if (withStemm) {
                Indexer.getInstance().setPathToPostFiles(postingPath + "/postingWithStemm");
                DocumentIndexer.getInstance().setPathToPostFolder(postingPath + "/postingWithStemm");
            } else {
                Indexer.getInstance().setPathToPostFiles(postingPath + "/postingNoStemm");
                DocumentIndexer.getInstance().setPathToPostFolder(postingPath + "/postingNoStemm");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    @Override
    public void runSearchQuery(String query,String corpusPath,boolean withSemantic) {
        List<String> result;
        ArrayList<String> resultAndId = new ArrayList<>();
        IR.Document queryDoc = new IR.Document(query,Integer.toString(id));
        id++;
        result = runSearch(queryDoc,corpusPath,withSemantic);
        //((ArrayList)result).add(Integer.toString(id));
        resultAndId.addAll(result);
        resultAndId.add(Integer.toString(id));
        queryRes =resultAndId;

        setChanged();
        notifyObservers(6);
    }



    @Override
    public void runSearchUsingFile(File fileToRead,String corpusPath,boolean withSemantic) {
        HashMap<String,List<String>> queryResult = new HashMap<>();
        Document doc = null;
        String id;

        try {
            doc = Jsoup.parse(fileToRead,"UTF8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements queries = doc.getElementsByTag("top");
        Elements q = doc.getAllElements();
        for (Element fileDoc : queries){
            String number= fileDoc.childNode(1).childNode(0).toString();
            id = number.substring(number.length()-4,number.length()-1);
            String query = fileDoc.getElementsByTag("title").text();
            IR.Document queryDoc = new IR.Document(query,id);
            List<String> result = runSearch(queryDoc,corpusPath,withSemantic);
            queryResult.put(id,result);

        }
        queryResFile = queryResult;
        setChanged();
        notifyObservers(5);
    }

    private List<String> runSearch(IR.Document query,String corpusPath,boolean withSemantic) {
        Searcher searcher = new Searcher(corpusPath);
        List<String> result = searcher.searchQuery(query, withSemantic);

        return result;
    }

    @Override
    public HashMap<String, List<String>> getqueryResUsingFile() {
        return queryResFile;
    }

    /**
     *Calling two function in each class and initialize all the object int the class
     */
    public  void resetObject(){
        Indexer.getInstance().resetIndexer();
        DocumentIndexer.getInstance().resetDocumentIndexer();
    }

    @Override
    public List<String> getQueryResUsingSearch() {
        return queryRes;
    }
}
