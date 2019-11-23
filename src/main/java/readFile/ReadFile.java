package readFile;

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
    public void readCorpus(File corpus){
        Document doc;

        for (File folder : corpus.listFiles()){
            if(folder.isDirectory()){
                readCorpus(folder);
            }
            else{
                try {
                    doc = Jsoup.parse(folder,"UTF8");
                    String body = doc.body().text();
                    //for (Element sentence : doc.getElementsByTag("DOCNO"))
                     //   System.out.print(sentence);
                   // System.out.println(doc.getElementsByTag("DOCNO").text());
                    System.out.println(folder.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}