package Parser;

import IR.Document;
import IR.Term;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.time.Month;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainParse extends AParser {
    String[] splitedText;
    Document d;
    public AtomicInteger i = new AtomicInteger(0);;
    String pattern = "(([0-9]+\\-[0-9]+)|([a-zA-Z]+-[a-zA-Z]+-[a-zA-Z]+)|([a-zA-Z]+-[a-zA-Z]+)|[0-9]+\\-[a-zA-Z]+)";
    Pattern pRange = Pattern.compile(pattern);
    Matcher matcherRange;
    static Semaphore docDequeuerLock;
    Document currentDoc;
    private DecimalFormat format3Decimals;

    public MainParse() {
        super();
        docDequeuerLock = new Semaphore(1);
        format3Decimals = new DecimalFormat("#.###");
    }

    @Override
    public void run() {
        System.out.println("Main Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Main Parser has stopped");

    }

    @Override
    public void parse() {
        docDequeuerLock.acquireUninterruptibly();
        currentDoc = dequeueDoc();
        i.set(0);
        docDequeuerLock.release();
        if(currentDoc != null)
        {
            parse(currentDoc);
        }

    }

    public void parse(Document document) {
        d = document;

        int m=0;
        System.out.println("There are " + docQueueWaitingForParse.size() + " left in the queue");
        splitedText = document.getTextArray();

        for (int index = 0; index < splitedText.length; index= i.getAndIncrement()) {

            String cleanWord = chopDownLastCharPunc(splitedText[index]);
            cleanWord = chopDownFisrtChar(cleanWord);

            //Check if thw word is a number
            if(!cleanWord.equals("")) {
                if (Character.isDigit(splitedText[index].charAt(0))) {
                    if (NumberUtils.isNumber(splitedText[index])) {
                        if (parsePercentage(cleanWord)) {

                        }
                        else if(parseNumbers(cleanWord))
                        {

                        }
                    } else {
                        if (parseNumberRanges(cleanWord)) {

                        }
                    }
                } else {
                    if (parseDates(cleanWord)) {

                    } else if (parseNameRanges(cleanWord)) {

                    }
                }
            }

        }

    }





    /*

        ParseDates

    */
    private boolean parseDates(String word) {
        boolean isParsed = false;
        if (!stopWords.contains(word)) {
            if (word != null && equalsMonth(word)) {
                String day;
                Term newTerm;
                String month;
                String year;
                int wordIndex = i.get();
                if (wordIndex > 0 && wordIndex < splitedText.length - 1) {
                    year = chopDownLastCharPunc(splitedText[wordIndex + 1]);
                    if (NumberUtils.isDigits(splitedText[wordIndex - 1])) {
                        month = String.format("%02d", getMonthNumber(word));
                        day = String.format("%02d", Integer.parseInt(splitedText[wordIndex - 1]));
                        parsedTermInsert(day + "-" + month, d.getDocNo());
                        //System.out.println(day + "-" + month);
                        isParsed = true;

                    }

                    if (NumberUtils.isDigits(year)) {
                        month = String.format("%02d", getMonthNumber(word));

                        //If the year is a day in the month
                        if (Integer.parseInt(year) <= 31) {
                            year = String.format("%02d", Integer.parseInt(year));
                            parsedTermInsert(month + "-" + year, d.getDocNo());
                            //System.out.println(month+"-"+year);
                            //newTerm = new Term(month +"-"+year);
                        } else {
                            parsedTermInsert(month + "-" + year, d.getDocNo());
                            //System.out.println(month+"-"+year);
                            //newTerm = new Term(year +"-"+month);
                        }
                        isParsed = true;
                        //i++;
                    }
                }
            }
        }
        return isParsed;
    }

    private int getMonthNumber(String monthName) {
        if (monthName.equalsIgnoreCase("Jan")) {
            monthName = "January";
        } else if (monthName.equalsIgnoreCase("Feb")) {
            monthName = "February";
        } else if (monthName.equalsIgnoreCase("Mar")) {
            monthName = "March";
        } else if (monthName.equalsIgnoreCase("Apr")) {
            monthName = "April";
        } else if (monthName.equalsIgnoreCase("Jun")) {
            monthName = "June";
        } else if (monthName.equalsIgnoreCase("Jul")) {
            monthName = "July";
        } else if (monthName.equalsIgnoreCase("Aug")) {
            monthName = "August";
        } else if (monthName.equalsIgnoreCase("Oct")) {
            monthName = "October";
        } else if (monthName.equalsIgnoreCase("Sept") || monthName.equalsIgnoreCase("Sep")) {
            monthName = "September";
        } else if (monthName.equalsIgnoreCase("Nov")) {
            monthName = "November";
        } else if (monthName.equalsIgnoreCase("Dec")) {
            monthName = "December";
        }
        return Month.valueOf(monthName.toUpperCase()).getValue();
    }


    private boolean equalsMonth(String word) {
        boolean isMonth = false;
        /*if(word.equals("jan") || word.equals("feb") || word.equals("mar") || word.equals("apr") || word.equals("may") || word.equals("jun") ||
                word.equals("jul") || word.equals("aug") || word.equals("sep") || word.equals("sept") || word.equals("oct") || word.equals("nov") ||
                word.equals("dec") || word.equals("january") || word.equals("february") || word.equals("march") || word.equals("april") || word.equals("november") ||
                word.equals("june") || word.equals("july") || word.equals("august") || word.equals("september") || word.equals("october") || word.equals("december")){
            return true;
        }
        return false;*/

        if (word.equalsIgnoreCase("Jan")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Feb")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Mar")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Apr")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("May")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Jun")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Jul")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Aug")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Oct")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Sept") || word.equalsIgnoreCase("Sep")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Nov")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("Dec")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("January")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("February")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("March")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("April")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("June")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("July")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("August")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("October")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("September")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("November")) {
            isMonth = true;
        } else if (word.equalsIgnoreCase("December")) {
            isMonth = true;
        }
        return isMonth;
    }



    /*

        Parse Percentages

    */
    public boolean parsePercentage(String word) {
        boolean isParsed = false;

        if (word != null && !stopWords.contains(word)) {

            if (word.length() > 0 && word.substring(word.length() - 1).equals("%")) {
                //if (word.length() > 0 && word.matches("\\b(?<!\\.)(?!0+(?:\\.0+)?%)(?:\\d|[1-9]\\d|100)(?:(?<!100)\\.\\d+)?%")) {
                //word = chopDownFisrtChar(word);
                if ((word.substring(0, word.length() - 1)).matches("^\\d+(\\.\\d+)?")) {
                    //double num = Double.parseDouble(word.substring(0, word.length() - 1));
                    parsedTermInsert(word, d.getDocNo());
                    isParsed = true;
                    //Term newTerm = new Term(word);
                    //System.out.println(word);
                } else if (isFraction(word.substring(0, word.length() - 1))) {
                    int wordIndex = i.get();
                    if (wordIndex > 2 && splitedText[wordIndex - 1].matches("^\\d+(\\.\\d+)?")) {
                        parsedTermInsert(splitedText[wordIndex - 1] + " " + word, d.getDocNo());
                        //Term newTerm = new Term(wordsInDoc[i - 1] + " " + word);
                        //System.out.println(splitedText[i - 1] + " " + word);
                    } else {
                        parsedTermInsert(word, d.getDocNo());
                        //Term newTerm = new Term(word);
                        //System.out.println(word);
                    }
                    isParsed = true;
                }
            } else if (word.equalsIgnoreCase("percentage") || word.equalsIgnoreCase("percent") ||
                    word.equalsIgnoreCase("percentages") || word.equalsIgnoreCase("percents")) {
                int wordIndex = i.get();
                if (wordIndex > 0) {
                    String lastWord = chopDownLastCharPunc(splitedText[wordIndex - 1]);
                    lastWord = chopDownFisrtChar(splitedText[wordIndex - 1]);

                    if (NumberUtils.isNumber(lastWord)) {
                        parsedTermInsert(lastWord + "%", d.getDocNo());
                        //Term newTerm = new Term(lastWord + "%");
                        //System.out.println(lastWord + "%");
                        isParsed = true;

                    } else if (isFraction(lastWord)) {
                        if (wordIndex > 2 && NumberUtils.isDigits(splitedText[wordIndex - 2])) {
                            parsedTermInsert(splitedText[wordIndex - 2] + " " + word, d.getDocNo());
                            //Term newTerm = new Term(wordsInDoc[i - 2] + " " + word);
                            //System.out.println(newTerm.getWordValue());
                        } else {
                            parsedTermInsert(word, d.getDocNo());
                            //System.out.println(newTerm.getWordValue());
                        }
                        isParsed = true;
                    }
                }
            }
        }
        return isParsed;
    }


    /*

        Parse Ranges

     */
    public boolean parseNameRanges(String word) {
        boolean isParsed = false;

       // if(!stopWords.contains(word)) {
            //  matcherRange = pRange.matcher(word);
            // while (matcherRange.find()) {

            //System.out.println(matcher.group(1));
            //String match = matcherRange.group(1);


            if (word.equals("between")) {

                int wordIndex = i.get();
                if (wordIndex < splitedText.length - 4) {
                    splitedText[wordIndex + 3] = chopDownLastCharPunc(splitedText[wordIndex + 3]);
                    if (splitedText[wordIndex + 2].equals("and") && NumberUtils.isNumber(splitedText[wordIndex + 1]) && NumberUtils.isNumber(splitedText[wordIndex + 3])) {
                        parsedTermInsert(splitedText[wordIndex + 1], d.getDocNo());
                        parsedTermInsert(splitedText[wordIndex + 3], d.getDocNo());
                        parsedTermInsert("between" + splitedText[wordIndex + 1] + "and" + splitedText[wordIndex + 3], d.getDocNo());
                        //System.out.println("between " + splitedText[i + 1] + " and " + splitedText[i + 3]);
                        isParsed = true;
                        i.addAndGet(3);
                    }
                }
            }

            if (!isParsed && !stopWords.contains(word)) {
                String[] values = word.split("--");
                //Kick out of the loop
                if (values.length > 1) {
                    return true;
                } else {
                    values = word.split("-");
                    if (values.length > 1) {
                        if (NumberUtils.isNumber(values[0]) && NumberUtils.isNumber(values[1])) {
                            parsedTermInsert(values[0], d.getDocNo());
                            parsedTermInsert(values[1], d.getDocNo());

                        }

                        //System.out.println(word);
                        isParsed = true;
                        parsedTermInsert(word, d.getDocNo());
                    }
                }
            }

        //}
        return isParsed;
    }

    public boolean parseNumberRanges(String word){
        boolean isParsed = false;

        // if(!stopWords.contains(word)) {
        //  matcherRange = pRange.matcher(word);
        // while (matcherRange.find()) {

        //System.out.println(matcher.group(1));
        //String match = matcherRange.group(1);
            String[] values = word.split("--");
            //Kick out of the loop
            if (values.length > 1) {
                return true;
            } else {
                values = word.split("-");
                if (values.length > 1) {
                    if (NumberUtils.isNumber(values[0]) && NumberUtils.isNumber(values[1])) {
                        parsedTermInsert(values[0], d.getDocNo());
                        parsedTermInsert(values[1], d.getDocNo());
                    }
                    //System.out.println(word);
                    isParsed = true;
                }
            }

        //}
        return isParsed;
    }


    public boolean parseNumbers(String word){

            word = chopDownLastCharPunc(word);
            word = chopDownFisrtChar(word);
            boolean isParsed = false;
            if (stopWords.contains(word.toLowerCase())) {
                return isParsed;
            }
            int wordIndex = i.get();
            if (word.matches("^\\d.*")) {
                if (wordIndex < splitedText.length - 1 && nextWordIsQuntifier(splitedText[wordIndex + 1])) {
                    String theWordParsed = quantifiedWordForDic(word, splitedText[wordIndex + 1]);
                    if (theWordParsed == null) {
                        //FUCK

                    } else {
                        parsedTermInsert(theWordParsed, currentDoc.getDocNo());
                        i.getAndIncrement();
                        isParsed = true;
                    }

                }


                if (word.matches("^\\d+(\\.\\d+)?-\\d+(\\.\\d+)?$")) {
                    String[] splitHifWord = word.split("-");
                    parsedTermInsert(splitHifWord[0], currentDoc.getDocNo());
                    parsedTermInsert(splitHifWord[1], currentDoc.getDocNo());

                    isParsed = true;
                }

                //TODO: this is related to טווחים וביטויים section
//                else if(word.matches("^\\d+-\\d+$"))
//                {
//                    countNumberMatch++;
////                    numbersInText.add(word.split("-")[0]);
////                    numbersInText.add(word.split("-")[1]);
//                    numbersInText.add(word);
//                }


                else if (word.matches("^\\d+/\\d+$")) {
                    parsedTermInsert(word, currentDoc.getDocNo());
                    isParsed = true;
                } else {
                    parsedTermInsert(quantifiedWordForDic(word), currentDoc.getDocNo());
                    isParsed = true;
                }


            }
            return isParsed;
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

