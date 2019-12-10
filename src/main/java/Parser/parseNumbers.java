package Parser;

import IR.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class parseNumbers extends AParser{


    //    private List<String> numbersInText;
//    private List<String> allNumbersInText;
//    private HashMap<String,Integer> allNumbersInText;
    private DecimalFormat format3Decimals;
    private Document currentDoc;
    private List<String> digits = new ArrayList<>();


    @Override
    public void run() {
        System.out.println("Num Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Num Parser has stopped");

    }


    public parseNumbers() {
        super();
//        allNumbersInText = new HashMap<>();
        parseName = "NumberParser";
        digits.add("0");
        digits.add("1");
        digits.add("2");
        digits.add("3");
        digits.add("4");
        digits.add("5");
        digits.add("6");
        digits.add("7");
        digits.add("8");
        digits.add("9");

        format3Decimals = new DecimalFormat("#.###");
    }


    @Override
    public void parse()
    {

        while(!queueIsEmpty())
        {
            Document d = dequeueDoc();
//            System.out.println("There are " + this.qSize() + " docs in the queue left");


            if(d == null)
            {
                continue;
            }

//        this.splitDocText(d);
            currentDoc = d;
            docText = d.getDocText().text().split(" ");

            int countNumberMatch=0,allNumbers=0;
            for (int wordIndex = 0; wordIndex < docText.length; wordIndex++) {
                String word = docText[wordIndex];
                if (stopWords.contains(word.toLowerCase())) {
                    continue;
                }
                word = chopDownLastCharPunc(word);
                word = chopDownFisrtChar(word);
                if (word.matches("^\\d.*")) {
                    if (wordIndex < docText.length - 1 && nextWordIsQuntifier(docText[wordIndex + 1]))
                    {/**  Number Quantifier  **/
                        String theWordParsed = quantifiedWordForDic(word, docText[wordIndex + 1]);
                        if (theWordParsed == null) {
                            //FUCK

                        } else {
                            countNumberMatch++;
                            parsedTermInsert(theWordParsed, currentDoc.getDocNo());
                            wordIndex++;
                            continue;
                        }

                    }


                    if (word.matches("^\\d+(\\.\\d+)?-\\d+(\\.\\d+)?$"))
                    {/**  num1-num2  **/
                        String[] splitHifWord = word.split("-");
                        parsedTermInsert(splitHifWord[0], currentDoc.getDocNo());
                        parsedTermInsert(splitHifWord[1], currentDoc.getDocNo());

                        continue;
                    }
                    else if (word.matches("^\\d+/\\d+$"))
                    {/**  num1/num2  **/
                        countNumberMatch++;
                        parsedTermInsert(word, currentDoc.getDocNo());
                        continue;
                    }
                    else{/**  num1  **/
                        countNumberMatch++;
                        parsedTermInsert(quantifiedWordForDic(word), currentDoc.getDocNo());
                    }


                }
            }
            numOfParsedDocInIterative++;
            this.releaseToIndexerFile();

        }


        //System.out.println("\n\n\n"+countNumberMatch+"/"+allNumbers);


    }

    /**
     *
     * @param word
     * @return
     */
    private boolean isWordNumber(String word) {
        try{
            Double d = Double.parseDouble(word);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * @param word - a string
     * @return true if the first char of the string is a digit
     */
    private boolean isWordStartsWithNumber(String word)
    {
        String firstD = word.charAt(0) +"";

        for (String d :
                digits) {
            if (d.equals(firstD))
            {
                return true;
            }
        }
        return false;
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
    protected String quantifiedWordForDic(String number,String quntifier) {
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
        else if(quntifier.equalsIgnoreCase("million") )
        {
            //Million
            result += "M";
        }
        else if(quntifier.equalsIgnoreCase("billion") )
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
    protected boolean nextWordIsQuntifier(String quntifier) {
        quntifier = chopDownLastCharPunc(quntifier);
        if(quntifier.matches("^(Thousand|Million|Billion)"))
        {
            return true;
        }
        return false;
    }





}
