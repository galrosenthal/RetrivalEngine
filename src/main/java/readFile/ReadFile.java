package readFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.File;
import java.io.IOException;

public class ReadFile {


    public void readCorpus(File corpus){
        Document doc;
        for (File folder : corpus.listFiles()){
            if(folder.isDirectory()){
                readCorpus(folder);
            }
            else{
                try {
                    doc = Jsoup.parse(folder,"UTF8");
                    String body = doc.getElementsByTag("TEXT").text();
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


    public static void main(String[] args) {

        File a = new File("C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\corpus\\FB396001\\FB396001");
        Document b;

        //readCorpus();
        {
            try {
                b = Jsoup.parse(a,"UTF8");
            } catch (IOException e) {

            }
        }
    }
}