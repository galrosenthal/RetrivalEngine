package Parser;

import IR.Document;

public class parseOther extends AParser {
    @Override
    public void parse() {
        while(!isQEmpty()) {
            int i = 0;
            Document document = dequeueDoc();

            String[] splitedText = document.getDocText().text().split(" ");
            for (String word: splitedText) {

            }
        }
    }

    @Override
    public void run() {
        System.out.println("Names Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Names Numbers is stopped");
    }
}
