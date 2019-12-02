package readFile;

import Indexer.Indexer;
import Parser.parseDates;
import Parser.parseNumbers;
import Parser.parsePercentage;
import Parser.parseRanges;
import Tokenizer.Tokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class ReadFile {
    public static int numOfTerms = 0;

    public static int numOfCorpusFiles = 0, numOfParsedDocs = 0;
    private Tokenizer theTokenizer = Tokenizer.getInstance();
    public parseNumbers prsNums = new parseNumbers();
    public parseDates prsDate = new parseDates();
    public parsePercentage prsPercent = new parsePercentage();
    public parseRanges prsRanges = new parseRanges();

    private Indexer myIndexer = Indexer.getInstance();
    private final int numberOfDocsToPost = 1000;

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
                        prsNums.parse(document);
                        if(numOfParsedDocs > numberOfDocsToPost)
                        {
                            myIndexer.enqueue(prsNums.getCopyOfNumbersInText());
                            prsNums.clearDic();
                            numOfParsedDocs = 0;
                        }
//                        parseDates pDate = new parseDates();
//                        pDate.parse(document);
                        //parsePercentage pp = new parsePercentage();
                        //pp.parse(document);
                        String[] wordsInDoc = document.getDocText().text().split(" ");
                        //prsDate.parse(wordsInDoc);
                        prsRanges.parse(document);
                        //prsPercent.parse(wordsInDoc);
                        //prsNums.parse(document);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        numOfTerms = prsRanges.getNumOfTerms();

    }

    public static int getNumOfTerms() {
        return numOfTerms;
    }
}