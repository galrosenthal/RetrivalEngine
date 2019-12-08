package Parser;

import IR.Document;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseNames extends AParser {
    String[] splitedText;
    String pattern = "([A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*)|([A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*)|([A-Z]+[a-z]*\\s[A-Z]+[a-z]*)|([A-Z]+[a-z]*)";
    Pattern p = Pattern.compile(pattern);
    Matcher matcher;

    @Override
    public void parse() {
        while(!queueIsEmpty()) {
            Document document = dequeueDoc();

            splitedText = document.getDocText().text().split("\\.");

            for (String line : splitedText) {
                matcher = p.matcher(line);
                while (matcher.find()) {
                    if(matcher.group(1)!=null){
                        parsedTermInsert(matcher.group(1),document.getDocNo());
                        String[] terms = matcher.group(1).split(" ");

                        for (String term :terms) {
                            parsedTermInsert(term,document.getDocNo());
                        }
                    }
                    else if(matcher.group(2)!=null){
                        parsedTermInsert(matcher.group(2),document.getDocNo());
                        String[] terms = matcher.group(2).split(" ");

                        for (String term :terms) {
                            parsedTermInsert(term,document.getDocNo());
                        }
                    }
                    else if(matcher.group(3)!=null){
                        parsedTermInsert(matcher.group(3),document.getDocNo());
                        String[] terms = matcher.group(3).split(" ");

                        for (String term :terms) {
                            parsedTermInsert(term,document.getDocNo());
                        }
                    }
                    else if(matcher.group(4)!=null){
                        parsedTermInsert(matcher.group(4),document.getDocNo());
                        String[] terms = matcher.group(4).split(" ");

                        for (String term :terms) {
                            parsedTermInsert(term,document.getDocNo());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {

    }
}
