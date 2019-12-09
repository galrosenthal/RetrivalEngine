package Parser;

import IR.Document;
import org.apache.commons.lang3.StringUtils;

public class parseWords extends AParser {
    String[] splitedText;

    public parseWords() {
        super();
    }

    @Override
    public void parse() {
        while(!queueIsEmpty()) {
            Document document = dequeueDoc();


            splitedText = StringUtils.split(document.getDocText().text(),' ');

            for (String word : splitedText) {
                word = chopDownFisrtChar(word);
                word = chopDownLastCharPunc(word);
                if (stopWords.contains(word.toLowerCase())) {
                    continue;
                }
                if(word.equals(word.toLowerCase())){
                    parsedTermInsert(word,document.getDocNo());
                }
            }
        }
    }
    @Override
    public void run() {
        System.out.println("Date Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Parsed Numbers is stopped");

    }
}
