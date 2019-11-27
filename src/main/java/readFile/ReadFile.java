package readFile;

import Parser.parseDates;
import Parser.parsePercentage;
import Parser.parseNumbers;
import Tokenizer.Tokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class ReadFile {

    public static int numOfCorpusFiles = 0;
    private Tokenizer theTokenizer = Tokenizer.getInstance();
    public parseNumbers prsNums = new parseNumbers();
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
                        IR.Document document = new IR.Document(fileDoc);
                        parseDates pDate = new parseDates();
                        pDate.parse(document);
                        parsePercentage pp = new parsePercentage();
                        pp.parse(document);
                        prsNums.parse(document);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




}