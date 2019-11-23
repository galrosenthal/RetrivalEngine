package Parser;

import IR.Document;

import java.lang.reflect.Array;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseDates extends AParser{
    String monthesPattern;

    public parseDates() {
        super();
        /*
        this.monthes = "January","Jan","March","Mar","February","Feb","April","Apr",
                "May","June","Jun","July","Jul","August","Aug","October","Oct","September","Sept","Sep","November","Nov",
                "December","Dec","JANUARY","JAN","FEBRUARY","FEB","MARCH","MAR","APRIL","APR","MAY","JUNE","JUN","JULY",
                "JUL","AUGUST","AUG","OCTOBER","OCT","SEPTEMBER","SEPT","SEP","NOVEMBER","NOV","DECEMBER","DEC";
        this.monthesPattern = "(January)|(Jan)|(March)|(Mar)|(February)|(Feb)|(April)|" +
                "(Apr)(May)|(June)|(Jun)|(July)|(Jul)|(August)|(Aug)|(October)"+
                "(Oct)|(September)|(Sept)|(Sep)|(November)|(Nov)|(December)|" +
                "(Dec)(JANUARY)|(JAN)|(FEBRUARY)|(FEB)|(MARCH)|(MAR)|(APRIL)|(APR)"+
                "(MAY)|(JUNE)|(JUN)|(JULY)|(JUL)|(AUGUST)|(AUG)|" +
                "(OCTOBER)(OCT)|(SEPTEMBER)|(SEPT)|(SEP)|(NOVEMBER)|(NOV)|(DECEMBER)|(DEC)";*/
        this.monthesPattern = "((^([0-3]\\d|\\d{3,}) ((January)|(Jan)|(March)|(Mar)|(February)|(Feb)|(April)|" +
                "(Apr)|(May)|(June)|(Jun)|(July)|(Jul)|(August)|(Aug)|(October)|(Oct)|(September)|(Sept)|(Sep)|" +
                "(November)|(Nov)|(December)|(Dec)|(JANUARY)|(JAN)|(FEBRUARY)|(FEB)|(MARCH)|(MAR)|(APRIL)|(APR)|" +
                "(MAY)|(JUNE)|(JUN)|(JULY)|(JUL)|(AUGUST)|(AUG)|(OCTOBER)|(OCT)|(SEPTEMBER)|(SEPT)|(SEP)|(NOVEMBER)|" +
                "(NOV)|(DECEMBER)|(DEC)))|(((January)|(Jan)|(March)|(Mar)|(February)|(Feb)|(April)|(Apr)|(May)|(June)|" +
                "(Jun)|(July)|(Jul)|(August)|(Aug)|(October)|(Oct)|(September)|(Sept)|(Sep)|(November)|(Nov)|(December)|" +
                "(Dec)|(JANUARY)|(JAN)|(FEBRUARY)|(FEB)|(MARCH)|(MAR)|(APRIL)|(APR)|(MAY)|(JUNE)|(JUN)|(JULY)|(JUL)|" +
                "(AUGUST)|(AUG)|(OCTOBER)|(OCT)|(SEPTEMBER)|(SEPT)|(SEP)|(NOVEMBER)|(NOV)|(DECEMBER)|(DEC)) \\d+))";
    }

    @Override
    public void parse(Document d) {
        int i=0;
        if(d != null){

            String docText = d.getDocText().text();
            Pattern pattern = Pattern.compile(monthesPattern);
            Matcher matcher = pattern.matcher(docText);

            while(matcher.find()){

                try {
                    System.out.println(matcher.group(1));


                }
                catch (NumberFormatException e)
                {

                }
                i++;
            }
        }
    }

    private int getMonthNumber(String monthName) {
        return Month.valueOf(monthName.toUpperCase()).getValue();
    }
}
