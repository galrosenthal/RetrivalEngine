package Parser;

import IR.Document;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.text.DecimalFormat;
import java.time.Month;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser.MainParse extends AParser and parse the following type of terms:
 * 1. Prices parser
 * 2. Dates parser
 * 3. Percentage Parser
 * 4. Number Parser
 * 5. NumberRanges Parser
 * 6. Slashes Parser
 * 7. Apostrophes Parser
 * 8. Emails Parser
 * 9. Phrases Parser
 * 10. Words Parser
 * 11. NameRanges Parser
 */
public class MainParse extends AParser {
    private String[] splitedText;
    private Document d;
    private AtomicInteger i = new AtomicInteger(0);
    //private String pattern = "(([0-9]+\\-[0-9]+)|([a-zA-Z]+-[a-zA-Z]+-[a-zA-Z]+)|([a-zA-Z]+-[a-zA-Z]+)|[0-9]+\\-[a-zA-Z]+)";
    //private Pattern pRange = Pattern.compile(pattern);
    //private Matcher matcherRange;
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
    }


    @Override
    public void run() {

        System.out.println("Main Parser has started");
        while (!stopThread) {
            parse();
        }
        System.out.println("Main Parser has stopped");

    }


    /**
     * This method called from the readfile class, dequeue from the queue the next document and call the parser to parse the document
     */
    @Override
    public void parse() {
        if(withStemm && snowballStemmer == null){
            snowballStemmer = new englishStemmer();
        }

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

    /**
     * The main method which runs all the parse methods, this method run each time on all the words in the specific document she
     * receives and decide which parser should he get by the decision tree.
     * @param document that being parse
     */
    public void parse(Document document) {
        termsInTextSemaphore.acquireUninterruptibly();
        d = document;
        isParsing = true;
        splitedText = document.getTextArray();

        //Runs all over the text of the document word by word and parse them
        for (int index = 0; index < splitedText.length; index = i.incrementAndGet()) {

            String cleanWord = chopDownLastCharPunc(splitedText[index]);
            cleanWord = chopDownFisrtChar(cleanWord);
            String halfCleanWord = chopDownFisrtChar(splitedText[i.get()]);

            //Check if we want to use stemming or not
            if(withStemm ){
                if(stopWords.contains(cleanWord)){
                    continue;
                }
                snowballStemmer.setCurrent(cleanWord);
                snowballStemmer.stem();
                cleanWord = snowballStemmer.getCurrent();
                snowballStemmer.setCurrent(halfCleanWord);
                snowballStemmer.stem();
                cleanWord = snowballStemmer.getCurrent();
            }

            //Check if the word is empty word
            if(!cleanWord.isEmpty() && !StringUtils.containsAny(cleanWord,"?|*&<>={}()�¥")){

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
                        else if(parseNumberRanges(cleanWord)){
                        }
                        else if(parseSlash(cleanWord)){

                        }
                        else{
                            parseNumbers(cleanWord);
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
                    else if(parseApostrophes(cleanWord)){

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
                    else if(parsePhrases(halfCleanWord)){
                    }
                    else {
                        parseWords(cleanWord);
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


    /**
     * Parse all the words which represent dates, in a form of day-month or month-year
     * @param word
     * @return true if the word parsed, false if not
     */
    private boolean parseDates(String word) {
        boolean isParsed = false;
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
                    parsedTermInsert(month + "-" + day, d,"Dates");
                    isParsed = true;

                }
                if (NumberUtils.isDigits(year)) {
                    month = String.format("%02d", getMonthNumber(word));

                    //If the year is a day in the month
                    if (Integer.parseInt(year) <= 31) {
                        year = String.format("%02d", Integer.parseInt(year));
                        parsedTermInsert(month + "-" + year, d,"Dates");

                    } else {
                        parsedTermInsert(year + "-" + month, d,"Dates");

                    }
                    isParsed = true;
                }
            }
        }
        return isParsed;
    }

    /**
     * Help function for the parseDates method, transform the month name to a number of the month
     * @param monthName a month
     * @return the number of the month
     */
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


    /**
     * Help function for the parseDates method, check if the word is a form of monthes
     * @param word
     * @return true if the word is one of the monthes
     */
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


    /**
     * Parse all the words have percent like "%" or eqauls to a form of percent
     * @param word to parse
     * @return true if the word parsed, false else
     */
    public boolean parsePercentage(String word) {
        boolean isParsed = false;

        if (word != null && !stopWords.contains(word)) {

            if (word.length() > 0 && word.substring(word.length() - 1).equals("%")) {

                if ((word.substring(0, word.length() - 1)).matches("^\\d+(\\.\\d+)?")) {
                    parsedTermInsert(word, d,"Precentage");
                    isParsed = true;

                } else if (isFraction(word.substring(0, word.length() - 1))) {
                    int wordIndex = i.get();
                    if (wordIndex > 2 && splitedText[wordIndex - 1].matches("^\\d+(\\.\\d+)?")) {
                        parsedTermInsert(splitedText[wordIndex - 1] + " " + word, d,"Precentage");

                    } else {
                        parsedTermInsert(word, d,"Precentage");
                    }
                    isParsed = true;
                }

             // The word is a form of the word percent
            } else if (word.equalsIgnoreCase("percentage") || word.equalsIgnoreCase("percent") ||
                    word.equalsIgnoreCase("percentages") || word.equalsIgnoreCase("percents")) {
                int wordIndex = i.get();
                if (wordIndex > 0 && wordIndex < splitedText.length) {
                    String lastWord = chopDownLastCharPunc(splitedText[wordIndex - 1]);
                    lastWord = chopDownFisrtChar(splitedText[wordIndex - 1]);

                    if (NumberUtils.isNumber(lastWord)) {
                        parsedTermInsert(lastWord + "%", d,"Precentage");

                        isParsed = true;

                    } else if (isFraction(lastWord)) {
                        if (wordIndex > 2 && NumberUtils.isDigits(splitedText[wordIndex - 2])) {
                            parsedTermInsert(splitedText[wordIndex - 2] + " " + lastWord +"%", d,"Precentage");
                            isParsed = true;
                        }else {
                            parsedTermInsert(lastWord +"%", d,"Precentage");
                            isParsed = true;
                        }
//                        else
//                        {
//                            parsedTermInsert(word, d,"Precentage");
//                        }
                    }
                }
            }
        }
        return isParsed;
    }


    /**
     *Parse the ranges numbers and words who have "-", for example "between 2 and 4"
     * @param word to parse
     * @return true if the word parsed, false else
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
                    parsedTermInsert("between " + splitedText[wordIndex + 1] + " and " + splitedText[wordIndex + 3], d,"NameRanges");
                    isParsed = true;
                    i.addAndGet(3);
                }
            }
        }


         //There is "-" the word
        if (!isParsed && !stopWords.contains(word)) {
            String[] values = word.split("--");
            //Kick out of the loop
            if (values.length > 1) {
                return true;
            } else {
                values = word.split("-");
                if (values.length > 1) {
                    for (String value:values) {
                        if (isAlphaBet(value) && !stopWords.contains(value)){
                            parseWords(value);
                        }
                    }

                    if(word.charAt(0)!= '$' && word.charAt(0)!= '#'){
                        parsedTermInsert(word, d,"NameRanges");
                    }

                    isParsed = true;

                }
            }
        }

        //}
        return isParsed;
    }

    /**
     * Parse all the words which represent range of numbers, for example if the word is 22-23, he saved the
     * word to dictionary and saved the numbers as terms
     * @param word to parse
     * @return true id the word has parsed, false else
     */
    public boolean parseNumberRanges(String word){
        boolean isParsed = false;

        String[] values = word.split("--");
        //Kick out of the loop
        if (values.length > 1) {
            return true;
        } else {
            values = word.split("-");
            if (values.length > 1) {

                for (String value:values) {
                    if (NumberUtils.isNumber(value) && NumberUtils.isNumber(value)){
                        parseNumbers(value);
                    }
                }

                if(word.charAt(0)!= '$'&& word.charAt(0)!= '#'){
                    parsedTermInsert(word, d,"NumberRanges");
                }

                isParsed = true;

            }
        }

        //}
        return isParsed;
    }


    /**
     * Parsing the word only if it is:
     * numbers only
     * double numbers
     * fraction numbers
     * @param word
     * @return true if the word was parsed by this parser
     */
    public boolean parseNumbers(String word){

        boolean isParsed = false;
        if (stopWords.contains(word.toLowerCase())) {
            return isParsed;
        }
        int wordIndex = i.get();

        if (isWordNumber(word))
        {/**searchong for word starting with number**/
            if (wordIndex < splitedText.length - 1 && nextWordIsQuntifier(splitedText[wordIndex + 1]))
            {/**searching for number and quantifier num1 (Thousand|Million|Billion) **/
                String theWordParsed = quantifiedWordForDic(word, splitedText[wordIndex + 1]);
                if (theWordParsed == null) {
                    //FUCK

                } else {

                    parsedTermInsert(theWordParsed, d, "Number");
                    i.getAndIncrement();
                    isParsed = true;
                }

            }
            else {
                parsedTermInsert(quantifiedWordForDic(word), d, "Number");
                isParsed = true;
            }
        }
        /**searches for fraction num1/num2**/
        else if (isFraction(word)) {
            parsedTermInsert(word, d,"Number");
            isParsed = true;
        }

        return isParsed;
    }

    /**
     * @param word a String
     * @return true if the word contains only numbers or \. for double numbers (5.34)
     */
    private boolean isWordNumber(String word) {
        word = word.replaceAll(",","");
        for (char d :
                word.toCharArray()) {
            if(!Character.isDigit(d))
            {
                if(d != '.')
                {
                    return false;
                }
            }
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

    /**
     * @param number a String to change into double
     * @return double of the number
     * @throws NumberFormatException
     */
    private double getNumberFromString(String number) throws NumberFormatException {
        number = chopDownLastCharPunc(number);
        double numberInString = 0.0;
        if(NumberUtils.isNumber(number.replaceAll(",","")))
        {
            numberInString = Double.parseDouble(number.replaceAll(",",""));
        }

        return numberInString;
    }


    /**
     * Parsing the word only if it is a Price.
     * @param word a String to parse if it is a Price
     * @return true if the word was parsed by this parser
     */
    public boolean parsePrices(String word) {

        currentDoc = d;
        docText = d.getTextArray();

        int wordIndex = i.get();
        String wordInText = docText[wordIndex];
        boolean isParsed = false;
        wordInText = chopDownLastCharPunc(wordInText);
        wordInText = chopDownFisrtChar(wordInText);
        if (stopWords.contains(wordInText.toLowerCase())) {
            return false;
        }
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
                    parsedTermInsert(termToInsert + " " + dollars,d,"Prices");
                    isParsed = true;
                    i.set(wordIndex+1);
                }
                else
                {/**$Price**/
                    String termToInsert = quantifiedWordForPrices(wordInText.substring(1));
                    parsedTermInsert(termToInsert + " " + dollars,d,"Prices");
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


    /**
     * @param quantifier a String to check if it a quantifier for prices or not
     * @return true if it is a quantifier for prices
     */
    private boolean nextWordIsQuntifier(String quantifier) {
        quantifier = chopDownLastCharPunc(quantifier);
        if(quantifier.equalsIgnoreCase("thousand") || quantifier.equalsIgnoreCase("million") ||
                quantifier.equalsIgnoreCase("billion") || quantifier.equalsIgnoreCase("trillion") ||
                quantifier.equalsIgnoreCase("m") || quantifier.equalsIgnoreCase("bn") )
        {
            return true;
        }
        return false;
    }


    /**
     * The parser which responsible to parse all the phrases,he passes all the words which start with upper case and
     * chain the to phrase,increment the index according to.
     * @param word to parse
     * @return true if it parsed the word,else false
     */
    public boolean parsePhrases(String word){
        boolean isParse = false;
        int numOfWords=0;
        StringBuilder wordB = new StringBuilder(word);
        StringBuilder sentence = new StringBuilder("");
        //wordB = chopDownLastCharPunc(wordB);
        while ((wordB.length()>0) && Character.isUpperCase(wordB.charAt(0))) {
            char lastChar = wordB.charAt(wordB.length()-1);

            if((lastChar < 'a' || lastChar > 'z') && (lastChar < 'A' || lastChar > 'Z')){
                sentence.append(chopDownLastCharPunc(wordB.toString()));

                if(numOfWords > 1 && numOfWords < 5){
                    parsedTermInsert(sentence.substring(0, sentence.length() - 1), d,"parsePhrases");
                    isParse = true;
                }
                numOfWords=0;
                sentence.setLength(0);
                break;
            } else {
                numOfWords++;
                sentence.append(wordB).append(" ");
            }
            if(i.get() < splitedText.length-1){
                wordB = new StringBuilder(splitedText[i.addAndGet(1)]);
                //wordB = chopDownFisrtChar(wordB);
            }
            else{
                break;
            }
        }
        if (numOfWords > 1) {

            parsedTermInsert(sentence.substring(0, sentence.length() - 1), d,"parsePhrases");
            numOfWords=0;
            sentence.setLength(0);
            isParse = true;
            i.decrementAndGet();
        }


        return isParse;
    }

    /**
     * The parser which parse one word, checks if the the word is alpha bet and not stop words ans enter it into the dictionary
     * @param word to parse
     * @return true if it parsed the word
     */
    public boolean parseWords(String word){
        boolean isParsed = false;
        StringBuilder wordB = new StringBuilder(word);

        if (wordB.length() < 2 || stopMWords.contains(wordB.toString().toLowerCase()) || wordB.toString().equals("") ||
                !isAlphaBet(wordB.toString())) {
            return isParsed;
        }

        else {

            parsedTermInsert(word,d,"parseWords");
            isParsed = true;
        }

        return isParsed;
    }

    /**
     * Check if the word is only alphabet
     * @param word to parse
     * @return true if it is aplhabet, false if not
     */
    public static boolean isAlphaBet(String word) {
        char[] chars = word.toCharArray();
        for(char c : chars){
            if(!(c>=65 && c<=90)&&!(c>=97 && c<=122) ){
                return false;
            }
        }
        return true;
    }

    /**
     * Parse words which they are emails
     * @param word to parse
     * @return true if the word is parsed. false else
     */
    public boolean parseEmails(String word){
        boolean isParsed = false;
        if(word.matches("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")){
            parsedTermInsert(word,d,"parseEmails");
            isParsed = true;
        }

        return isParsed;
    }

    /**
     * Parse words with / inside, the parser split the word by / and save the words as
     * two diffrent words
     * @param word to parse
     * @return true if the parser parse the word
     */
    public boolean parseSlash(String word){
        boolean isParsed = false;

        if(containsSlash(word)){

            String [] splitWord = word.split("/");
            for(String wordIn: splitWord) {
                if(isAlphaBet(wordIn) && !stopWords.contains(wordIn)){
                    parseWords(wordIn);
                    isParsed = true;
                }
                else if(NumberUtils.isNumber(wordIn)){
                    parseNumbers(wordIn);
                }
            }
        }

        return isParsed;
    }

    /**
     * The function check if there is / in the word
     * @param word we want to parse
     * @return true if there is / in the word
     */
    public boolean containsSlash(String word){

        char[] chars = word.toCharArray();
        for(char c : chars){
            if(c == '/'){
                return true;
            }
        }
        return false;
    }

    /**
     * Parse all the world with 's or ' at the end.
     * The parser remove the ' as follow
     * @param word word from the document
     * @return if the parser succeeded to parse
     */
    public boolean parseApostrophes(String word){
        boolean isParsed = false;
        StringBuilder wordB = new StringBuilder(word);
        if(!wordB.toString().isEmpty() && !stopWords.contains(wordB) && wordB.length()> 2) {
            if (wordB.charAt(wordB.length() - 1) == '\'') {
                parsedTermInsert(wordB.substring(0, wordB.length() - 1), d, "parseApostrophes");
                isParsed = true;
            } else if (wordB.charAt(wordB.length() - 2) == '\'' && wordB.charAt(wordB.length() - 1) == 's') {
                parsedTermInsert(wordB.substring(0, wordB.length() - 2), d, "parseApostrophes");
                isParsed = true;
            }
        }

        return isParsed;
    }
}

