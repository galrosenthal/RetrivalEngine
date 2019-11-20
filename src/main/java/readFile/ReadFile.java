package readFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

public class ReadFile {

    public static void main(String[] args) {

        File a = new File("C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\corpus\\FB396001\\FB396001");
        Document b;

        {
            try {
                b = Jsoup.parse(a,"UTF8");
            } catch (IOException e) {

            }
        }

    }

}
