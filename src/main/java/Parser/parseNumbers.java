package Parser;

import IR.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseNumbers extends AParser {

    private final double BILLION = 1000000000;
    private final double MILLION = 1000000;
    private final double THOUSAND = 1000;
    private List<String> numbersInText;

    public parseNumbers() {
        numbersInText = new ArrayList<>();
    }

    @Override
    public void parse(Document d) {

//        this.splitDocText(d);


        Pattern numPattern = Pattern.compile("\\d+\\/\\d+|\\d+\\.\\d+|(\\d+\\ (Thousand|Million|Billion))|^\\d{1,3},\\d{3}$|^\\d{1,3},\\d{3},\\d{3}$|^\\d{1,3},\\d{3},\\d{3},\\d{3}$|\\d+\\/\\d+|\\d+\\ \\d+\\/\\d+|\\d+$");
        Matcher numMatcher = numPattern.matcher(d.getDocText().text());

        while(numMatcher.find())
        {
            numbersInText.add(numMatcher.group());
        }


        System.out.println("Test");



    }
}
