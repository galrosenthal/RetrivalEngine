package Parser;

import IR.Document;
import IR.Term;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.Month;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseDates extends AParser{
    public static int numOfTerms = 0;
    String monthesFirstPattern;
    String dayFirstPattern;

    public static int getNumOfTerms() {
        return numOfTerms;
    }

    Pattern pattern;
    Matcher matcher;

    public parseDates() {
        super();

        this.dayFirstPattern = "(((january)|(jan)|(march)|(mar)|(february)|(feb)|(april)|(apr)|(may)|(june)|(jun)|" +
                "(july)|(jul)|(august)|(aug)|(october)|(oct)|(september)|(sept)|(sep)|(november)|(nov)|(december)|(dec)))";
        //pattern = Pattern.compile(dayFirstPattern);
        //matcher = pattern.matcher(word.toLowerCase());
    }

    @Override
    public void run() {
        System.out.println("Date Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Date Parser has stopped");

    }

    @Override
    public void parse() {
        while(!queueIsEmpty())
        {
            Document d = dequeueDoc();
            if(d == null)
            {
                continue;
            }
            if(d != null){
                int i = 0;
                String docText = d.getDocText().text();
                //matcher = pattern.matcher(docText);
                String[] wordsInDoc = d.getDocText().text().split(" ");
                for (String word :
                        wordsInDoc) {
                    //word = chopDownFisrtChar(word);
                    //word = chopDownLastCharPunc(word);

                if(word != null && equalsMonth(word)){
                    String day;
                    Term newTerm;
                    String month;
                    String year;

                    if(i > 0 && i < wordsInDoc.length-1){
                        if(NumberUtils.isDigits(wordsInDoc[i-1])){
                            month = String.format("%02d",getMonthNumber(word));
                            day = String.format("%02d",Integer.parseInt(wordsInDoc[i-1]));
                            newTerm = new Term(day+"-"+month);
                            numOfTerms++;
                            //System.out.println(newTerm.getWordValue());
                        }

                        year = chopDownLastCharPunc(wordsInDoc[i+1]);
                        if(NumberUtils.isDigits(year)){
                            month = String.format("%02d",getMonthNumber(word));

                            //If the year is a day in the month
                            if(Integer.parseInt(year) <= 31){
                                year = String.format("%02d",Integer.parseInt(year));
                                newTerm = new Term(month +"-"+year);
                                numOfTerms++;
                            }
                            else{
                                newTerm = new Term(year +"-"+month);
                                numOfTerms++;
                            }

                            //System.out.println(newTerm.getWordValue());
                            //System.out.println(month);
                        }

                    }
                }

                i++;
            }
            //  while(matcher.find()){
            //System.out.println(matcher.group());
            //}


                /**
                 * Finds all the dates with numbers in the beginning
                 */
            /*
            Pattern pattern = Pattern.compile(dayFirstPattern);
            Matcher matcher = pattern.matcher(docText);
            findAllMatches(matcher);
            while(matcher.find()){
                try {
                    String day;
                    String month;
                    String fixDay;
                    Term newTerm;
                    if(matcher.group(52) != null){
                        day = matcher.group(52).substring(0,matcher.group(52).length()-matcher.group(53).length()-1);
                        month = String.format("%02d",getMonthNumber(matcher.group(53)));
                        fixDay = String.format("%02d",Integer.parseInt(day));
                        newTerm = new Term(fixDay+"-"+month);
                        //System.out.println(newTerm.getWordValue());
                    }`

                    if(matcher.group(1) != null){
                        day = matcher.group(1).substring(matcher.group(3).length()+1);
                        month = String.format("%02d",getMonthNumber(matcher.group(3)));
                        fixDay = String.format("%02d",Integer.parseInt(day));
                        newTerm = new Term(fixDay+"-"+month);
                        //System.out.println(newTerm.getWordValue());
                    }

                }
                catch (NumberFormatException e)
                {

                }
            }
            */

                /**
                 * Finds all the dates with the months at the beginning
                 */
            /*
            pattern = Pattern.compile(monthesFirstPattern);
            matcher = pattern.matcher(docText);
            findAllMatches(matcher);
            while(matcher.find()){
                try {
                    String day = matcher.group(0).substring(matcher.group(3).length()+1);
                    String month = String.format("%02d",getMonthNumber(matcher.group(3)));
                    String fixDay = String.format("%02d",Integer.parseInt(day));
                    Term newTerm = new Term(fixDay+"-"+month);
                }
                catch (NumberFormatException e)
                {

                }
            }*/
            }

        }
    }

    private int getMonthNumber(String monthName) {
        if(monthName.equalsIgnoreCase("Jan")){
            monthName = "January";
        }
        else if(monthName.equalsIgnoreCase("Feb")){
            monthName = "February";
        }
        else if (monthName.equalsIgnoreCase("Mar")){
            monthName = "March";
        }
        else if (monthName.equalsIgnoreCase("Apr")){
            monthName = "April";
        }
        else if(monthName.equalsIgnoreCase("Jun")){
            monthName = "June";
        }
        else if(monthName.equalsIgnoreCase("Jul")){
            monthName = "July";
        }
        else if(monthName.equalsIgnoreCase("Aug")){
            monthName = "August";
        }
        else if(monthName.equalsIgnoreCase("Oct")){
            monthName = "October";
        }
        else if(monthName.equalsIgnoreCase("Sept") || monthName.equalsIgnoreCase("Sep")){
            monthName = "September";
        }
        else if(monthName.equalsIgnoreCase("Nov")){
            monthName = "November";
        }
        else if(monthName.equalsIgnoreCase("Dec")){
            monthName = "December";
        }
        return Month.valueOf(monthName.toUpperCase()).getValue();
    }


    private boolean equalsMonth(String word){
        boolean isMonth = false;
        /*if(word.equals("jan") || word.equals("feb") || word.equals("mar") || word.equals("apr") || word.equals("may") || word.equals("jun") ||
                word.equals("jul") || word.equals("aug") || word.equals("sep") || word.equals("sept") || word.equals("oct") || word.equals("nov") ||
                word.equals("dec") || word.equals("january") || word.equals("february") || word.equals("march") || word.equals("april") || word.equals("november") ||
                word.equals("june") || word.equals("july") || word.equals("august") || word.equals("september") || word.equals("october") || word.equals("december")){
            return true;
        }
        return false;*/

        if(word.equalsIgnoreCase("Jan")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Feb")){
            isMonth = true;
        }
        else if (word.equalsIgnoreCase("Mar")){
            isMonth = true;
        }
        else if (word.equalsIgnoreCase("Apr")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("May")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Jun")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Jul")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Aug")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Oct")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Sept") || word.equalsIgnoreCase("Sep")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Nov")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("Dec")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("January")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("February")){
            isMonth = true;
        }
        else if (word.equalsIgnoreCase("March")){
            isMonth = true;
        }
        else if (word.equalsIgnoreCase("April")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("June")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("July")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("August")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("October")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("September")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("November")){
            isMonth = true;
        }
        else if(word.equalsIgnoreCase("December")){
            isMonth = true;
        }
        return isMonth;
    }


}
