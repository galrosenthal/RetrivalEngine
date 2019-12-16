package Parser;

import IR.Document;
import org.apache.commons.lang3.math.NumberUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.text.DecimalFormat;
import java.time.Month;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainParse extends AParser {
    private String[] splitedText;
    private Document d;
    private AtomicInteger i = new AtomicInteger(0);
    private String pattern = "(([0-9]+\\-[0-9]+)|([a-zA-Z]+-[a-zA-Z]+-[a-zA-Z]+)|([a-zA-Z]+-[a-zA-Z]+)|[0-9]+\\-[a-zA-Z]+)";
    private Pattern pRange = Pattern.compile(pattern);
    private Matcher matcherRange;
    private static Semaphore docDequeuerLock;
    private Document currentDoc = null;
    private DecimalFormat format3Decimals;
    private final String dollars = "Dollars";
    private final String us = "U.S.";
    private SnowballStemmer snowballStemmer;




    public MainParse() {
        super();

        this.parseName = "Main Parser";
        docDequeuerLock = new Semaphore(1);
        format3Decimals = new DecimalFormat("#.###");



//        i = new AtomicInteger(0);
    }


    @Override
    public void run() {

        System.out.println("Main Parser has started");
        while (!stopThread) {
            parse();
        }
        System.out.println("Main Parser has stopped");

    }

    @Override
    public void parse() {
        if(withStemm && snowballStemmer == null){
            snowballStemmer = new englishStemmer();
        }
//            while (currentDoc == null) {
//                currentDoc = dequeueDoc();
//            }
//        System.out.println("There are " + docQueueWaitingForParse.size() + " left in the queue");
        docDequeuerLock.acquireUninterruptibly();
        currentDoc = dequeueDoc();
        if (currentDoc == null) {
            docDequeuerLock.release();
            return;
        }
        docDequeuerLock.release();
        i.set(0);
        parse(currentDoc);
        makeDocParsed(currentDoc);
        numOfParsedDocInIterative++;
        releaseToIndexerFile();
    }

    public void parse(Document document) {
        termsInTextSemaphore.acquireUninterruptibly();
        d = document;
        isParsing = true;
//        i.set(0);
//        currentDoc = d;

        //System.out.println("There are " + docQueueWaitingForParse.size() + " left in the queue");
        splitedText = document.getTextArray();

        for (int index = 0; index < splitedText.length; index = i.incrementAndGet()) {

            String cleanWord = chopDownLastCharPunc(splitedText[index]);
            cleanWord = chopDownFisrtChar(cleanWord);
            String halfCleanWord = chopDownFisrtChar(splitedText[i.get()]);

            if(withStemm){
                snowballStemmer.setCurrent(cleanWord);
                snowballStemmer.stem();
                cleanWord = snowballStemmer.getCurrent();
                snowballStemmer.setCurrent(halfCleanWord);
                snowballStemmer.stem();
                cleanWord = snowballStemmer.getCurrent();
            }

            //Check if the word is empty word
            if(!cleanWord.isEmpty()){

                //Check if the first char is number
                if(Character.isDigit(cleanWord.charAt(0))){

                    //Check if the all word is a number
                    if(NumberUtils.isNumber(cleanWord)){
                        if(parsePrices(cleanWord)){

                        }
                        else if(parseNumbers(cleanWord)){

                        }
                    }
                    //If the the word is not a all number
                    else{
                        if(parsePercentage(cleanWord)){

                        }
                        //Check fractions
                        else if(isFraction(cleanWord)){
                            parseNumbers(cleanWord);
                        }
                        else{
                            parseNumberRanges(cleanWord);
                        }
                    }
                }
                else if(!isAlphaBet(cleanWord)){
                    if(parseNameRanges(cleanWord)){

                    }
                    //Check if the char is $
                    else if(cleanWord.charAt(0) == '$'){
                        parsePrices(cleanWord);
                    }
                    else if(parseSlash(cleanWord)){

                    }
                    else{
                        parseEmails(cleanWord);
                    }
                }
                //The first letter is a character and upper case
                else if(Character.isUpperCase(cleanWord.charAt(0))){
                    if(parseDates(cleanWord)){

                    }
                    else if(parsePercentage(cleanWord)){

                    }
                    else{
                        parsePhrases(halfCleanWord);
                    }
                }
                else{
                    if(parseDates(cleanWord)){

                    }
                    else if(parsePercentage(cleanWord)){

                    }
                    else if(parseNameRanges(cleanWord)){

                    }
                    else {
                        if(parseWords(cleanWord)){

                        }
                    }
                }
            }
        }
        termsInTextSemaphore.release();
        isParsing = false;
    }




    /*

        ParseDates

    */
    private boolean parseDates(String word) {
        boolean isParsed = false;
        if (!stopWords.contains(word))
        {
            if (word != null && equalsMonth(word)) {
                String day;
//                Term newTerm;
                String month;
                String year;
                int wordIndex = i.get();
                if (wordIndex > 0 && wordIndex < splitedText.length - 1) {
                    year = chopDownLastCharPunc(splitedText[wordIndex + 1]);
                    if (NumberUtils.isDigits(splitedText[wordIndex - 1])) {
                        month = String.format("%02d", getMonthNumber(word));
                        day = String.format("%02d", Integer.parseInt(splitedText[wordIndex - 1]));
                        parsedTermInsert(day + "-" + month, d,"Dates");
                        //System.out.println(day + "-" + month);
                        isParsed = true;

                    }else if (NumberUtils.isDigits(year)) {
                        month = String.format("%02d", getMonthNumber(word));

                        //If the year is a day in the month
                        if (Integer.parseInt(year) <= 31) {
                            year = String.format("%02d", Integer.parseInt(year));
                            parsedTermInsert(month + "-" + year, d,"Dates");
                            //System.out.println(month+"-"+year);
                            //newTerm = new Term(month +"-"+year);
                        } else {
                            parsedTermInsert(month + "-" + year, d,"Dates");
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
                    parsedTermInsert(word, d,"Precentage");
                    isParsed = true;
                    //Term newTerm = new Term(word);
                    //System.out.println(word);
                } else if (isFraction(word.substring(0, word.length() - 1))) {
                    int wordIndex = i.get();
                    if (wordIndex > 2 && splitedText[wordIndex - 1].matches("^\\d+(\\.\\d+)?")) {
                        parsedTermInsert(splitedText[wordIndex - 1] + " " + word, d,"Precentage");
                        //Term newTerm = new Term(wordsInDoc[i - 1] + " " + word);
                        //System.out.println(splitedText[i - 1] + " " + word);
                    } else {
                        parsedTermInsert(word, d,"Precentage");
                        //Term newTerm = new Term(word);
                        //System.out.println(word);
                    }
                    isParsed = true;
                }
            } else if (word.equalsIgnoreCase("percentage") || word.equalsIgnoreCase("percent") ||
                    word.equalsIgnoreCase("percentages") || word.equalsIgnoreCase("percents")) {
                int wordIndex = i.get();
                if (wordIndex > 0 && wordIndex < splitedText.length) {
                    String lastWord = chopDownLastCharPunc(splitedText[wordIndex - 1]);
                    lastWord = chopDownFisrtChar(splitedText[wordIndex - 1]);

                    if (NumberUtils.isNumber(lastWord)) {
                        parsedTermInsert(lastWord + "%", d,"Precentage");
                        //Term newTerm = new Term(lastWord + "%");
                        //System.out.println(lastWord + "%");
                        isParsed = true;

                    } else if (isFraction(lastWord)) {
                        if (wordIndex > 2 && NumberUtils.isDigits(splitedText[wordIndex - 2])) {
                            parsedTermInsert(splitedText[wordIndex - 2] + " " + word, d,"Precentage");
                            //Term newTerm = new Term(wordsInDoc[i - 2] + " " + word);
                            //System.out.println(newTerm.getWordValue());
                        } else {
                            parsedTermInsert(word, d,"Precentage");
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

        if (word.equals("between")) {

            int wordIndex = i.get();
            if (wordIndex < splitedText.length - 4) {
                splitedText[wordIndex + 3] = chopDownLastCharPunc(splitedText[wordIndex + 3]);
                if (splitedText[wordIndex + 2].equals("and") && NumberUtils.isNumber(splitedText[wordIndex + 1]) && NumberUtils.isNumber(splitedText[wordIndex + 3])) {
                    parsedTermInsert(splitedText[wordIndex + 1], d,"NameRanges");
                    parsedTermInsert(splitedText[wordIndex + 3], d,"NameRanges");
                    parsedTermInsert("between" + splitedText[wordIndex + 1] + "and" + splitedText[wordIndex + 3], d,"NameRanges");
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
                        parsedTermInsert(values[0], d,"NameRanges");
                        parsedTermInsert(values[1], d,"NameRanges");

                    }

                    //System.out.println(word);
                    isParsed = true;
                    parsedTermInsert(word, d,"NameRanges");
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
                    parsedTermInsert(values[0], d,"NumberRanges");
                    parsedTermInsert(values[1], d,"NumberRanges");
                }
                //System.out.println(word);
                isParsed = true;
            }
        }

        //}
        return isParsed;
    }


    /*

        Parse Numbers

     */
    public boolean parseNumbers(String word){

        word = chopDownLastCharPunc(word);
        word = chopDownFisrtChar(word);

        boolean isParsed = false;
        if (stopWords.contains(word.toLowerCase())) {
            return isParsed;
        }
        int wordIndex = i.get();
        if (Character.isDigit(word.charAt(0)))
        {/**searchong for word starting with number**/
            if (wordIndex < splitedText.length - 1 && nextWordIsQuntifier(splitedText[wordIndex + 1]))
            {/**searching for number and quantifier num1 (Thousand|Million|Billion) **/
                String theWordParsed = quantifiedWordForDic(word, splitedText[wordIndex + 1]);
                if (theWordParsed == null) {
                    //FUCK

                } else {

                    parsedTermInsert(theWordParsed, d,"Number");
                    i.getAndIncrement();
                    isParsed = true;
                }

            }

//                /**searches for num1-num2**/
//                if (word.matches("^\\d+(\\.\\d+)?-\\d+(\\.\\d+)?$")) {
//                    String[] splitHifWord = word.split("-");
//                    parsedTermInsert(splitHifWord[0], d);
//                    parsedTermInsert(splitHifWord[1], d);
//
//                    isParsed = true;
//                }
            /**searches for fraction num1/num2**/
            else if (isFraction(word)) {
                parsedTermInsert(word, d,"Number");
                isParsed = true;
            }
            else {
                /**parsing number**/
                if(allCharsAreDigits(word)) {
                    parsedTermInsert(quantifiedWordForDic(word), d, "Number");
                    isParsed = true;
                }
            }


        }
        return isParsed;
    }

    private boolean allCharsAreDigits(String word) {
        for (char c :
                word.toCharArray()) {
            if(!Character.isDigit(c))
                return false;
        }
        return true;
    }


    /**
     * Gets a number as string
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
            e.printStackTrace();
            return null;
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
        quntifier = chopDownFisrtChar(quntifier);
        number = chopDownLastCharPunc(number);
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
//        if(number.matches("^(\\d+|\\d{1,3}(,\\d{3})*)(\\.\\d+)?$"))
        if(NumberUtils.isNumber(number.replaceAll(",","")))
        {
            numberInString = Double.parseDouble(number.replaceAll(",",""));
//                    System.out.println(word);
        }

        return numberInString;
    }

    //    /**
//     * Checks whether or not the quntifier is a related one (Thousand,Million,Billion)
//     * @param quntifier
//     * @return
//     */
//    protected boolean nextWordIsQuntifier(String quntifier) {
//        quntifier = chopDownLastCharPunc(quntifier);
//        quntifier = chopDownFisrtChar(quntifier);
//        if(quntifier.matches("^(Thousand|Million|Billion)"))
//        {
//            return true;
//        }
//        return false;
//    }
    public boolean parsePrices(String word) {

//        this.splitDocText(d);
        currentDoc = d;
        docText = d.getTextArray();

        int countNumberMatch = 0, allNumbers = 0;
        int wordIndex = i.get();
        String wordInText = docText[wordIndex];
        boolean isParsed = false;
        wordInText = chopDownLastCharPunc(wordInText);
        wordInText = chopDownFisrtChar(wordInText);
        if (stopWords.contains(wordInText.toLowerCase())) {
            return false;
        }
//        if (wordInText.matches("(^\\d.*)"))
        if (NumberUtils.isNumber(word.charAt(0)+""))
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
                        i.set(wordIndex+1);
                    }
                    // if there is no dollars after U.S. ignore all
                    return false;
                }

                if(nextWordIsQuntifier(isQuantifier) && scndWord.equalsIgnoreCase(dollars.toLowerCase()))
                {//It is Quntifier {Thousand,Million,Billion}
                    termToInsert = quantifiedWordForPrices(wordInText,isQuantifier);
                    /**Price Quantifier Dollars**/
                    termToInsert += " " + dollars;
                    parsedTermInsert(termToInsert, d,"Prices");
                    isParsed = true;
                    i.set(wordIndex+2);
                }
                else if(isFraction(isQuantifier) && scndWord.equalsIgnoreCase(dollars.toLowerCase()))
                {
                    /**Price Fraction Dollars**/
                    termToInsert = quantifiedWordForPrices(wordInText);
                    termToInsert = " " + isQuantifier + " " + dollars;
                    parsedTermInsert(termToInsert, d,"Prices");
                    isParsed = true;
                    i.set(wordIndex+2);
                }
                else if(isQuantifier.equalsIgnoreCase(dollars.toLowerCase()))
                {
                    /**Price Dollars**/
                    termToInsert = quantifiedWordForPrices(wordInText);
                    termToInsert = " " + dollars;
                    parsedTermInsert(termToInsert, d,"Prices");
                    isParsed = true;
                    i.set(wordIndex+1);
                }
            }
        }
        else if(word.length()>1 && word.charAt(0) == '$' && NumberUtils.isNumber(word.charAt(1)+""))
        {//$price | regex: \$\d.*
            if(wordIndex < docText.length-1)
            {
                String quant = docText[wordIndex+1];
                if(nextWordIsQuntifier(quant))
                {/**$Price Quantifier**/
                    String termToInsert = quantifiedWordForPrices(wordInText.substring(1),quant);
                    parsedTermInsert("$"+ termToInsert,d,"Prices");
                    isParsed = true;
                    i.set(wordIndex+1);
                }
                else
                {/**$Price**/
                    String termToInsert = quantifiedWordForPrices(wordInText.substring(1));
                    parsedTermInsert("$"+termToInsert,d,"Prices");
                    isParsed = true;
                    i.set(wordIndex+1);
                }
            }

        }
        return isParsed;
    }


    /**
     * Returns the number with the quantifier attached if the qunatifier is larger than Million
     * @param number
     * @param quantifier
     * @return
     */
    private String quantifiedWordForPrices(String number, String quantifier) {
        String theQuantifier = "";
//        if(quantifier.matches("^(Million|Billion|Trillion|m|bn|trillion|million|billion)"))

        if(quantifier.equalsIgnoreCase("million") ||quantifier.equalsIgnoreCase("billion") ||
                quantifier.equalsIgnoreCase("trillion") || quantifier.equalsIgnoreCase("m") ||
                quantifier.equalsIgnoreCase("bn") )
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
    private String quantifiedWordForPrices(String number)
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

//    private double getNumberFromString(String number) throws NumberFormatException {
//        number = chopDownLastCharPunc(number);
//        double numberInString = 0.0;
//        if(number.matches("^(\\d+|\\d{1,3}(,\\d{3})*)(\\.\\d+)?$"))
//        {
//            numberInString = Double.parseDouble(number.replaceAll(",",""));
////                    System.out.println(word);
//        }
//
//        return numberInString;
//    }

    private boolean nextWordIsQuntifier(String quantifier) {
        quantifier = chopDownLastCharPunc(quantifier);
//        if(quantifier.matches("^(Thousand|Million|Billion|Trillion|m|bn|trillion|million|bilion)"))
        if(quantifier.equalsIgnoreCase("thousand") || quantifier.equalsIgnoreCase("million") ||
                quantifier.equalsIgnoreCase("billion") || quantifier.equalsIgnoreCase("trillion") ||
                quantifier.equalsIgnoreCase("m") || quantifier.equalsIgnoreCase("bn") )
        {
            return true;
        }
        return false;
    }


     /*

        Parse Names

     */

    public boolean parsePhrases(String word){
        boolean isParse = false;
        int numOfWords=0;
        StringBuilder wordB = new StringBuilder(word);
        StringBuilder sentence = new StringBuilder("");
        //wordB = chopDownLastCharPunc(wordB);
        while ((wordB.length()>0) && Character.isUpperCase(wordB.charAt(0))) {
            char lastChar = wordB.charAt(wordB.length()-1);

            if((lastChar < 'a' || lastChar > 'z') && (lastChar < 'A' || lastChar < 'Z')){
                sentence.append(chopDownLastCharPunc(wordB.toString()));

                //String[] sentenceLengh = sentence.toString().split(" ");
                if(numOfWords > 1 && numOfWords < 5){
                    parsedTermInsert(sentence.substring(0, sentence.length() - 1), d);
                    isParse = true;
                }
                numOfWords=0;
                sentence.setLength(0);
                break;
            } else {
                numOfWords++;
                sentence.append(wordB).append(" ");
                //parsedTermInsert(wordB.toString(), d);
            }
            if(i.get() < splitedText.length-1){
                wordB = new StringBuilder(splitedText[i.addAndGet(1)]);
                wordB = chopDownFisrtChar(wordB);
            }
            else{
                break;
            }
        }
        if (numOfWords > 1) {
            //String[] sentenceLengh = sentence.toString().split(" ");

            //System.out.println(sentence);
            parsedTermInsert(sentence.substring(0, sentence.length() - 1), d);
            numOfWords=0;
            sentence.setLength(0);
            isParse = true;
        }


        return isParse;
    }

    /*

        Parse Words

     */
    public boolean parseWords(String word){
        boolean isParsed = false;
        //System.out.println(word);
        StringBuilder wordB = new StringBuilder(word);

        if (wordB.length() < 3 || stopMWords.contains(wordB.toString().toLowerCase()) || wordB.toString().equals("") ) {
            return isParsed;
        }
        //else if (wordB.toString().chars().allMatch(Character::isLetter)){
        else {
            //System.out.println(word);
            parsedTermInsert(word,d);
            isParsed = true;
        }

        return isParsed;
    }

    public static boolean isAlphaBet(String word) {
        char[] chars = word.toCharArray();
        for(char c : chars){
            if(!(c>=65 && c<=90)&&!(c>=97 && c<=122) ){
                return false;
            }
        }
        return true;
    }

    /*

        Parse emails

     */
    public boolean parseEmails(String word){
        boolean isParsed = false;
        if(word.matches("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")){
            parsedTermInsert(word,d);
            isParsed = true;
        }

        return isParsed;
    }

    /*

        Parse Slash

     */
    public boolean parseSlash(String word){
        boolean isParsed = false;

        if(containsSlash(word)){

            String [] splitWord = word.split("/");
            for(String wordIn: splitWord) {
                if(isAlphaBet(wordIn) && !stopWords.contains(wordIn)){
                    parsedTermInsert(wordIn,d);
                    isParsed = true;
                }

            }
        }

        return isParsed;
    }

    public boolean containsSlash(String word){

        char[] chars = word.toCharArray();
        for(char c : chars){
            if(c == '/'){
                return true;
            }
        }
        return false;
    }
}

