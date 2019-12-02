package Parser;

import IR.Document;

import java.text.DecimalFormat;
import java.util.HashMap;

public class parseNumbers extends AParser {

    private final double BILLION = 1000000000;
    private final double MILLION = 1000000;
    private final double THOUSAND = 1000;
//    private List<String> numbersInText;
//    private List<String> allNumbersInText;
    private HashMap<String,Integer> allNumbersInText;
    private DecimalFormat format3Decimals;
    private Document currentDoc;

    public parseNumbers() {
        super();
        allNumbersInText = new HashMap<>();
        format3Decimals = new DecimalFormat("#.###");
    }

    @Override
    public void parse(Document d)
    {

//        this.splitDocText(d);
        currentDoc = d;
        docText = d.getDocText().text().split(" ");

        int countNumberMatch=0,allNumbers=0;
        for (int wordIndex = 0; wordIndex < docText.length; wordIndex++) {
            String word = docText[wordIndex];
            if(stopWords.contains(word.toLowerCase()))
            {
                continue;
            }
            word = chopDownLastCharPunc(word);
            word = chopDownFisrtChar(word);
            if(word.matches("^\\d.*")) {
                if(wordIndex < docText.length-1 && nextWordIsQuntifier(docText[wordIndex+1]))
                {
                    String theWordParsed = quantifiedWordForDic(word,docText[wordIndex + 1]);
                    if(theWordParsed == null)
                    {
                        //FUCK

                    }
                    else
                    {
                        countNumberMatch++;
                        parsedTermInsert(theWordParsed,currentDoc.getDocNo());
                        wordIndex++;
                        continue;
                    }

                }


                if (word.matches("^\\d+(\\.\\d+)?-\\d+(\\.\\d+)?$"))
                {
                    String[] splitHifWord = word.split("-");
                    parsedTermInsert(splitHifWord[0],currentDoc.getDocNo());
                    parsedTermInsert(splitHifWord[1],currentDoc.getDocNo());

                    continue;
                }

                //TODO: this is related to טווחים וביטויים section
//                else if(word.matches("^\\d+-\\d+$"))
//                {
//                    countNumberMatch++;
////                    numbersInText.add(word.split("-")[0]);
////                    numbersInText.add(word.split("-")[1]);
//                    numbersInText.add(word);
//                }


                else if(word.matches("^\\d+/\\d+$"))
                {
                    countNumberMatch++;
                    parsedTermInsert(word,currentDoc.getDocNo());
                    continue;
                }
                else
                {
                    countNumberMatch++;
                    parsedTermInsert(quantifiedWordForDic(word),currentDoc.getDocNo());
                }


            }
        }

        //System.out.println("\n\n\n"+countNumberMatch+"/"+allNumbers);


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
        if(importantNumber < THOUSAND)
        {
            importantNumber = importantNumber;
            result = format3Decimals.format(importantNumber);
        }
        else if(importantNumber < MILLION)
        {
            importantNumber = importantNumber/THOUSAND;

            result = format3Decimals.format(importantNumber);
            result += "K";
        }
        else if(importantNumber < BILLION)
        {
            importantNumber = importantNumber/MILLION;
            result = format3Decimals.format(importantNumber);
            result += "M";
        }
        else
        {
            importantNumber = importantNumber/BILLION;
            result = format3Decimals.format(importantNumber);
            result += "B";
        }

        return result;
    }

    /**
     * Gets 2 Strings one is a number and the second is the quntifier (Thousand,Million,Billion)
     * and
     * Returns a String with number formatted as desired in the Instructions (Thousand->K,Million->M...)
     * @param number
     * @param quntifier
     * @return
     */
    private String quantifiedWordForDic(String number,String quntifier) {
        quntifier = chopDownLastCharPunc(quntifier);
        number = chopDownLastCharPunc(number);

        double importantNumber;
        try {
            importantNumber = getNumberFromString(number);
        }
        catch (Exception e)
        {
            return null;
        }
        String result = importantNumber+"";
        if(quntifier.equalsIgnoreCase("thousand"))
        {
            //Thousand
            result += "K";

        }
        else if(quntifier.equalsIgnoreCase("million"))
        {
            //Million
            result += "M";
        }
        else if(quntifier.equalsIgnoreCase("billion"))
        {
            //billion
            result += "B";
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

    /**
     * Checks whether or not the quntifier is a related one (Thousand,Million,Billion)
     * @param quntifier
     * @return
     */
    private boolean nextWordIsQuntifier(String quntifier) {
        quntifier = chopDownLastCharPunc(quntifier);
        if(quntifier.matches("^(Thousand|Million|Billion)"))
        {
            return true;
        }
        return false;
    }



    /**
     * @return the Dictionary of this parser
     */
    public HashMap<String, String> getCopyOfNumbersInText() {
        return new HashMap<String,String>(termsInText);
    }


}
