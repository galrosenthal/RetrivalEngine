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

                    Elements docs = doc.getElementsByTag("doc");
                    for (Element fileDoc:
                            docs) {
                        numOfCorpusFiles++;
                        theTokenizer.tokenizingText(fileDoc.getElementsByTag("docno"),fileDoc.getElementsByTag("text"));
                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}