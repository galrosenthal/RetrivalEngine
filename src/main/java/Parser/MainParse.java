package Parser;

import IR.Document;
import IR.Term;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.Month;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainParse extends AParser {
    String[] splitedText;
    Document d;
    public static int i;
    String pattern = "(([0-9]+\\-[0-9]+)|([a-zA-Z]+-[a-zA-Z]+-[a-zA-Z]+)|([a-zA-Z]+-[a-zA-Z]+)|[0-9]+\\-[a-zA-Z]+)";
    Pattern pRange = Pattern.compile(pattern);
    Matcher matcherRange;

    @Override
    public void parse() {

    }

    public void parse(Document document) {
        d = document;
        i = 0;
        int m=0;
        splitedText = document.getDocText().text().split(" ");

        for (int index = 0; index < splitedText.length; index=i) {

            String cleanWord = chopDownLastCharPunc(splitedText[index]);
            cleanWord = chopDownFisrtChar(cleanWord);


            if (parseDates(cleanWord)) {

            }
            else if (parsePercentage(cleanWord)) {

            }
            else if(parseRanges(cleanWord)){

            }
            i++;
        }

    }


    @Override
    public void run() {

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

                if (i > 0 && i < splitedText.length - 1) {
                    year = chopDownLastCharPunc(splitedText[i + 1]);
                    if (NumberUtils.isDigits(splitedText[i - 1])) {
                        month = String.format("%02d", getMonthNumber(word));
                        day = String.format("%02d", Integer.parseInt(splitedText[i - 1]));
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
                    if (i > 2 && splitedText[i - 1].matches("^\\d+(\\.\\d+)?")) {
                        parsedTermInsert(splitedText[i - 1] + " " + word, d.getDocNo());
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
                if (i > 0) {
                    String lastWord = chopDownLastCharPunc(splitedText[i - 1]);
                    lastWord = chopDownFisrtChar(splitedText[i - 1]);

                    if (NumberUtils.isNumber(lastWord)) {
                        parsedTermInsert(lastWord + "%", d.getDocNo());
                        //Term newTerm = new Term(lastWord + "%");
                        //System.out.println(lastWord + "%");
                        isParsed = true;

                    } else if (isFraction(lastWord)) {
                        if (i > 2 && NumberUtils.isDigits(splitedText[i - 2])) {
                            parsedTermInsert(splitedText[i - 2] + " " + word, d.getDocNo());
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
    public boolean parseRanges(String word) {
        boolean isParsed = false;

       // if(!stopWords.contains(word)) {
            //  matcherRange = pRange.matcher(word);
            // while (matcherRange.find()) {

            //System.out.println(matcher.group(1));
            //String match = matcherRange.group(1);


            if (word.equals("between")) {

                if (i < splitedText.length - 4) {
                    splitedText[i + 3] = chopDownLastCharPunc(splitedText[i + 3]);
                    if (splitedText[i + 2].equals("and") && NumberUtils.isNumber(splitedText[i + 1]) && NumberUtils.isNumber(splitedText[i + 3])) {
                        parsedTermInsert(splitedText[i + 1], d.getDocNo());
                        parsedTermInsert(splitedText[i + 3], d.getDocNo());
                        parsedTermInsert("between" + splitedText[i + 1] + "and" + splitedText[i + 3], d.getDocNo());
                        //System.out.println("between " + splitedText[i + 1] + " and " + splitedText[i + 3]);
                        isParsed = true;
                        i += 3;
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
}
