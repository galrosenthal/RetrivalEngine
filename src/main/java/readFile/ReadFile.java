package readFile;

import Indexer.*;
import Parser.AParser;
import Parser.MainParse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * readFile.ReadFile Class is reading all the documents from the corpus
 * it has 5 parameters:
 * numOfParsedDocs - Counts how many documents are in the corpus
 * withStemm - A boolean thats represents whether or not the parse should use stemming or not
 * allParserThreads - List of Threads for each parser
 * allParsers - List of parsers
 * mainParse{1..4} - 4 parsers
 */
public class ReadFile {

    public static int numOfParsedDocs;
    private boolean withStemm;

    private List<Thread> allParserThreads;
    private List<AParser> allParsers;

    private MainParse mainParse1 = new MainParse();
    private MainParse mainParse2 = new MainParse();
    private MainParse mainParse3 = new MainParse();
    private MainParse mainParse4 = new MainParse();
    private String corpusPath;

    public void setCorpusPath(String corpusPath) {
        mainParse1.setPathToCorpus(corpusPath);
    }

    public ReadFile(boolean withStemm) {
        this.withStemm = withStemm;
        allParserThreads = new ArrayList<>();
        allParsers = new ArrayList<>();
        addParserToThreads(mainParse1);
        addParserToThreads(mainParse2);
        addParserToThreads(mainParse3);
        addParserToThreads(mainParse4);
        numOfParsedDocs = 0;
        runParsers();
    }



    private void addParserToThreads(AParser prsr) {
        allParsers.add(prsr);
        prsr.setStemm(withStemm);
        allParserThreads.add(new Thread(prsr));
    }

    public void runParsers() {
        for (Thread t :
                allParserThreads) {
            t.start();
        }
    }

    public void stopThreads()
    {
        while(!allPrsrQsEmpty())
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (AParser prsr :
                allParsers) {
            prsr.stopThread();
        }

        try
        {
            for (Thread t :
                    allParserThreads) {
                t.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        while(!Indexer.getInstance().isQEmpty())
        {

        }
        Indexer.stopThreads = true;
        DocumentIndexer.stopThreads = true;

    }

    private boolean allPrsrQsEmpty() {
        for (AParser prsr :
                allParsers) {
            if (!prsr.isQEmpty())
            {
                return false;
            }
        }
        return true;
    }


    /**
     * This Functions get a File which can be a directory or a file of the Corpus
     * and creates a IR.Document for each document inside the file
     * and then enqueues it into the parsers queue
     * @param corpus -  a file or folder of the Corpus
     * @see IR.Document
     */
    public void readCorpus(File corpus){
        Document doc;

        for (File folder : corpus.listFiles()){
            if(folder.isDirectory()){
                readCorpus(folder);
            }
            else{
                try {
                    doc = Jsoup.parse(folder,"UTF8");
                    Elements docs = doc.getElementsByTag("doc");
                    for (Element fileDoc :
                            docs) {
                        numOfParsedDocs++;
                        IR.Document document = new IR.Document(fileDoc);
                        enqDocToAllParsers(document);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * Enqueues a document to the Static Q of the parsers
     * @param document - a document to enqueue for the parsers
     */
    private void enqDocToAllParsers(IR.Document document) {
//        for (AParser prsr :
//                allParsers) {
//            prsr.enqueueDoc(document);
//        }
        allParsers.get(0).enqueueDoc(document);
    }

}