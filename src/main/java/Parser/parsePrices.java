package Parser;

import IR.Document;

import java.text.DecimalFormat;

public class parsePrices extends AParser {


    private Document currentDoc;
    private final String dollars="Dollars";
    private final String us="U.S.";
    private DecimalFormat format3Decimals;

    public parsePrices() {
        super();
        parseName = "PriceParser";
        this.format3Decimals = new DecimalFormat("#.###");;
    }

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
                if (word.matches("(^\\d.*)"))
                {//Current word is a number
                    if(wordIndex < docText.length-4)
                    {//Check next word for quntifier
                        String isQuantifier = docText[wordIndex+1];
                        String scndWord = docText[wordIndex+2];
                        String thrdWord = docText[wordIndex+3];
                        String termToInsert = "";
                        if(scndWord.equalsIgnoreCase(us.toLowerCase()))
                        {/**Price Quntifier U.S. Dollars**/
                            // if there is U.S. in the sentence ignore it
                            if(thrdWord.equalsIgnoreCase(dollars.toLowerCase()))
                            {
                                //if the 3rd word is dollars to get exactly this situation
                                scndWord = thrdWord;
                            }
                            // if there is no dollars after U.S. ignore all
                            continue;
                        }

                        if(nextWordIsQuntifier(isQuantifier) && scndWord.equalsIgnoreCase(dollars.toLowerCase()))
                        {//It is Quntifier {Thousand,Million,Billion}
                            termToInsert = quantifiedWordForDic(word,isQuantifier);
                            /**Price Quantifier Dollars**/
                            termToInsert += " " + dollars;
                            parsedTermInsert(termToInsert, currentDoc.getDocNo());
                            wordIndex += 2;
                        }
                        else if(isFraction(isQuantifier) && scndWord.equalsIgnoreCase(dollars.toLowerCase()))
                        {
                            /**Price Fraction Dollars**/
                            termToInsert = quantifiedWordForDic(word);
                            termToInsert = " " + isQuantifier + " " + dollars;
                            parsedTermInsert(termToInsert, currentDoc.getDocNo());
                            wordIndex += 2;
                        }
                        else if(isQuantifier.equalsIgnoreCase(dollars.toLowerCase()))
                        {
                            /**Price Dollars**/
                            termToInsert = quantifiedWordForDic(word);
                            termToInsert = " " + dollars;
                            parsedTermInsert(termToInsert, currentDoc.getDocNo());
                            wordIndex += 1;
                        }
                    }
                }
                else if(word.matches(("^\\$\\d+")))
                {//$price
                    if(wordIndex < docText.length-1)
                    {
                        String quant = docText[wordIndex+1];
                        if(nextWordIsQuntifier(quant))
                        {/**$Price Quantifier**/
                            String termToInsert = quantifiedWordForDic(word.substring(1),quant);
                            parsedTermInsert("$"+ termToInsert,currentDoc.getDocNo());
                        }
                        else
                        {/**$Price**/
                            String termToInsert = quantifiedWordForDic(word.substring(1));
                            parsedTermInsert("$"+termToInsert,currentDoc.getDocNo());
                        }
                    }

                }
            }
            numOfParsedDocInIterative++;
            this.releaseToIndexerFile();
        }

    }

    /**
     * Returns the number with the quantifier attached if the qunatifier is larger than Million
     * @param number
     * @param quantifier
     * @return
     */
    private String quantifiedWordForDic(String number, String quantifier) {
        String theQuantifier = "";
        if(quantifier.matches("^(Million|Billion|Trillion|m|bn|trillion|million|billion)"))
        {
            theQuantifier = "M";
            number += theQuantifier;
        }
        return number;
    }

    /**
     * Gets a String with a number
     * and
     * Returns a String with number formatted as desired in the Instructions (Thousand->K,Million->M...)
     * @param number
     * @return
     */
    private String quantifiedWordForDic(String number)
    {
        number = chopDownLastCharPunc(number);
        double importantNumber;

        String result = "";
        try
        {
            importantNumber = getNumberFromString(number);
        }
        catch (Exception e)
        {
            return result;
        }
        result = format3Decimals.format(importantNumber);
        if(importantNumber >= MILLION)
        {
            importantNumber = importantNumber/MILLION;

            result+=" M";
        }

        return result;
    }

    private double getNumberFromString(String number) throws NumberFormatException {
        number = chopDownLastCharPunc(number);
        double numberInString = 0.0;
        if(number.matches("^(\\d+|\\d{1,3}(,\\d{3})*)(\\.\\d+)?$"))
        {
            numberInString = Double.parseDouble(number.replaceAll(",",""));
//                    System.out.println(word);
        }

        return numberInString;
    }

    private boolean nextWordIsQuntifier(String quantifier) {
        quantifier = chopDownLastCharPunc(quantifier);
        if(quantifier.matches("^(Thousand|Million|Billion|Trillion|m|bn|trillion|million|bilion)"))
        {
            return true;
        }
        return false;
    }

    private String generatePrice(String word, String quntifier, String dlrStr)
    {

        return null;
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
