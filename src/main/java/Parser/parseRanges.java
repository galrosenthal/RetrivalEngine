package Parser;

import IR.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseRanges extends AParser {
    String pattern = "(([0-9]\\-[0-9])|([[:alpha:]]+-[[:alpha:]]+))";
    Pattern p = Pattern.compile(pattern);
    Matcher matcher;
    StringBuilder text;
    int numOfTerms = 0;

    @Override
    public void parse(String[] wordsInDoc) {

    }

    public void parse(Document document) {
        try {
            text= new StringBuilder(document.getDocText().text());
            matcher = p.matcher(text);

            while (matcher.find()){
                numOfTerms++;
                //System.out.println(matcher.group(1));
            }

        } catch (Exception e) {

        }
    }

    public int getNumOfTerms() {
        return numOfTerms;
    }
}
