package Parser;

import IR.Document;

public class parsePrices extends AParser {


    private Document currentDoc;



    @Override
    public void parse() {
        while(!queueIsEmpty()) {
            Document d = dequeueDoc();
//            System.out.println("There are " + this.qSize() + " docs in the queue left");


            if (d == null) {
                continue;
            }

//        this.splitDocText(d);
            currentDoc = d;
            docText = d.getDocText().text().split(" ");

            int countNumberMatch = 0, allNumbers = 0;
            for (int wordIndex = 0; wordIndex < docText.length; wordIndex++) {
                String word = docText[wordIndex];
                if (stopWords.contains(word.toLowerCase())) {
                    continue;
                }
                word = chopDownLastCharPunc(word);
                word = chopDownFisrtChar(word);
                //TODO: if the word is number check the next word for quantity(Million/Billion), and then check for dollars
                // Price Dollars
                // Price Fraction Dollars
                // $price
                // $price million/biliion
                // Price m Dollars
                // Price bn Dollars
                // Price billion U.S. dollars
                // Price million U.S. dollars
                // Price trillion U.S. dollars
                if (word.matches("(^\\d.*)")) {

                }
                //TODO: if the word starts with dollar check if next word is number and quantity(Million/Billion)
                else if(word.matches(("\\$|dollars|Dollars|DOLLARS")))
                {

                }
            }
        }

    }

    @Override
    public void run() {
        System.out.println("Price Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Price Parser has stopped");

    }
}
