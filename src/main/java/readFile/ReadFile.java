package readFile;

import Parser.AParser;
import Parser.MainParse;
import Tokenizer.Tokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import Indexer.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadFile {

    private int numOfParsers = 0;
    private static final int DOC_CREATED_IN_QS = 10000;
    public static int numOfCorpusFiles = 0, numOfParsedDocs = 0;
    private Tokenizer theTokenizer = Tokenizer.getInstance();
//    public Thread prsNumThrd ;
//    public Thread prsDatesThrd ;
//    public Thread prsPrcntThrd;
//    public Thread prsPriceThrd;
    public List<Thread> allParserThreads;
    public List<AParser> allParsers;
//    public parsePrices prsPrices = new parsePrices();
//    public parseNumbers prsNums = new parseNumbers();
//    public parseDates prsDates = new parseDates();
//    public parsePercentage prsPrcntg = new parsePercentage();
//    public parseNames prsNames = new parseNames();
//    public parseRanges prsRanges = new parseRanges();
//    public parseWords prsWords = new parseWords();
//    public parseOther prsOther = new parseOther();
    public MainParse mainParse1 = new MainParse();
    public MainParse mainParse2 = new MainParse();
    public MainParse mainParse3 = new MainParse();
    public MainParse mainParse4 = new MainParse();
//    private Indexer myIndexer = Indexer.getInstance();
//    private final int numberOfDocsToPost = 1000;

    public ReadFile() {
        allParserThreads = new ArrayList<>();
        allParsers = new ArrayList<>();
        addParserToThreads(mainParse1);
        addParserToThreads(mainParse2);
        addParserToThreads(mainParse3);
        addParserToThreads(mainParse4);
        //addParserToThreads(prsNums);
        //addParserToThreads(prsDates);
        //addParserToThreads(prsPrcntg);
//        addParserToThreads(prsPrices);
        //addParserToThreads(prsNames);
//         addParserToThreads(prsRanges);
        //addParserToThreads(prsWords);
        //addParserToThreads(prsOther);
        runParsers();

    }

    private void addParserToThreads(AParser prsr) {
        allParsers.add(prsr);
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
//        while(!Indexer.getInstance().isQEmpty())
//        {
//
//        }
        Indexer.stopThreads = true;

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

    public void releaseAllParsedTermsToIndex()
    {
        mainParse1.stopThread();
    }

    public void readCorpus(File corpus){
        Document doc;

        for (File folder : corpus.listFiles()){
            if(folder.isDirectory()){
                readCorpus(folder);
            }
            else{
                try {
                    doc = Jsoup.parse(folder,"UTF8");
//                    String body = doc.body().text();
                    //for (Element sentence : doc.getElementsByTag("DOCNO"))
                    //   System.out.print(sentence);
                    // System.out.println(doc.getElementsByTag("DOCNO").text());
                    Elements docs = doc.getElementsByTag("doc");
                    for (Element fileDoc :
                            docs) {
                        numOfCorpusFiles++;
                        numOfParsedDocs++;
                        IR.Document document = new IR.Document(fileDoc);
                        enqDocToAllParsers(document);
//                        System.out.println("Starting parse " + document.getDocNo());
//                        mainParse1.enqueueDoc(document);
//                        mainParse1.parse();
//                        System.out.println("Finished parse " + document.getDocNo());
                        //prsPrcntg.parse();

//                        new Thread(()-> prsNums.parse(document)).start();
//                        if(numOfParsedDocs > numberOfDocsToPost)
//                        {
//                            myIndexer.enqueue(prsNums.getCopyOfTermInText());
//                            prsNums.clearDic();
//                            numOfParsedDocs = 0;
//                        }
//                        parseDates pDate = new parseDates();
//                        pDate.parse(document);
                        //parsePercentage pp = new parsePercentage();
                        //pp.parse(document);
                        //prsNums.parse(document);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //mainParse1.parse();

    }

    public void runParse()
    {
        mainParse1.parse();
    }

    private void enqDocToAllParsers(IR.Document document) {
        for (AParser prsr :
                allParsers) {
            prsr.enqueueDoc(document);
        }
    }

    private void shouldWaitForParser() {
        if(!allPrsrQsEmpty())
        {
            try
            {
                Thread.sleep(5);
            }
            catch (Exception e)
            {

            }
        }
    }

    public void reset() {
        numOfParsedDocs = 0;
        numOfCorpusFiles = 0;
        mainParse1 = new MainParse();
    }
}