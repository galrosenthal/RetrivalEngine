package Parser;

import IR.Document;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseRanges extends AParser {
    String pattern = "(([0-9]+\\-[0-9]+)|(between [0-9]+ and [0-9]+)|([[:alpha:]]+-[[:alpha:]]+-[[:alpha:]]+)|([[:alpha:]]+-[[:alpha:]]+))";
    Pattern p = Pattern.compile(pattern);
    Matcher matcher;
    int numOfTerms = 0;
    String[] splitedText;

    @Override
    public void run() {
        System.out.println("Ranges Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Ranges Parser has stopped");

    }

    @Override
    public void parse() {
        while(!isQEmpty()) {
            Document document = dequeueDoc();
            if (document == null) {
                continue;
            }
            try {
                splitedText = document.getDocText().text().split("\\r?\\n");

                for (String line : splitedText) {
                    matcher = p.matcher(line);
                    while (matcher.find()) {

                        //System.out.println(matcher.group(1));
                        String match = matcher.group(1);

                        String[] values = StringUtils.split(match, '-');
                        if (values[0].matches("^\\d+") && values[1].matches("^\\d+")) {
                            parsedTermInsert(values[1], document);
                            parsedTermInsert(match, document);

                        } else {
                            String[] words = StringUtils.split(match, ' ');
                            if (words.length > 3 && words[0].equals("between") && words[2].equals("and")) {
                                parsedTermInsert(words[1], document);
                                parsedTermInsert(words[3], document);

                            }
                        }
                        System.out.println(match);
                        parsedTermInsert(match, document);
                    }
                }

            } catch (Exception e) {

            }
        }
    }

    public int getNumOfTerms() {
        return numOfTerms;
    }

    @Override
    public void clearDic() {

    }
}
