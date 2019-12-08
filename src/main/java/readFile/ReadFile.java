package readFile;

import Indexer.Indexer;
import Parser.parseDates;
import Parser.parseNames;
import Parser.parseNumbers;
import Parser.parsePercentage;
import Tokenizer.Tokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class ReadFile {

    private static final int MAX_NUMBER_OF_THREADS = 4;
    private static final int DOC_CREATED_IN_QS = 50000;
    public static int numOfCorpusFiles = 0, numOfParsedDocs = 0;
    private Tokenizer theTokenizer = Tokenizer.getInstance();
    public parseNumbers prsNums = new parseNumbers();
    public Thread prsNumThrd ;
    public parseDates prsDates = new parseDates();
    public Thread prsDatesThrd ;
    public Parser.parsePercentage prsPrcntg = new parsePercentage();
    public Thread prsPrcntThrd;
    parseNames prsNames = new parseNames();
//    private Indexer myIndexer = Indexer.getInstance();
//    private final int numberOfDocsToPost = 1000;

    public ReadFile() {
        runParsers();

    }

    private void runParsers() {
        prsNumThrd = new Thread(prsNums);
        prsNumThrd.start();

        prsDatesThrd = new Thread(prsDates);
        prsDatesThrd.start();

        prsPrcntThrd = new Thread(prsPrcntg);
        prsPrcntThrd.start();

    }

    public void stopThreads()
    {
        while(!prsNums.isQEmpty() && !Indexer.getInstance().isQEmpty() && !prsDates.isQEmpty() && !prsPrcntg.isQEmpty())
        {
        }
        //prsNums.stopThread();
        //prsDates.stopThread();
        //prsPrcntg.stopThread();


        Indexer.stopThreads = true;
        try
        {
            prsNumThrd.join();
            prsDatesThrd.join();
            prsPrcntThrd.join();
        }
        catch (Exception e)
        {

        }

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
                        prsNums.enqueueDoc(document);
                        prsDates.enqueueDoc(document);
                        prsPrcntg.enqueueDoc(document);
                        prsNames.enqueueDoc(document);
                        prsNames.parse();
                        shouldWaitForParser();

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

    }

    private void shouldWaitForParser() {
        if(prsNums.qSize() >= DOC_CREATED_IN_QS)
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


}