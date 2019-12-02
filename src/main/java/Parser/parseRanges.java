package Parser;

import IR.Document;
import IR.Term;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.helper.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseRanges extends AParser {
    String pattern = "((([0-9]+\\-[0-9]+)|([[:alpha:]]+-[[:alpha:]]+))|(between [0-9]+ and [0-9]+))";
    Pattern p = Pattern.compile(pattern);
    Matcher matcher;
    StringBuilder text;
    int numOfTerms = 0;

    @Override
    public void parse(Document document) {
        try {
            text= new StringBuilder(document.getDocText().text());
            matcher = p.matcher(text);

            while (matcher.find()){
                numOfTerms++;
                //System.out.println(matcher.group(1));
                String match = matcher.group(1);
                String[] values = StringUtils.split(match,'-');
                if(NumberUtils.isDigits(values[0]) && NumberUtils.isDigits(values[2])){

                }

               // newTerm = new Term(match);

            }

        } catch (Exception e) {

        }
    }

    public int getNumOfTerms() {
        return numOfTerms;
    }

    @Override
    public void clearDic() {

    }
}
