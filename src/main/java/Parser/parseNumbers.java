package Parser;

import IR.Document;

import java.util.ArrayList;
import java.util.List;

public class parseNumbers extends AParser {

    private final double BILLION = 1000000000;
    private final double MILLION = 1000000;
    private final double THOUSAND = 1000;
    private List<String> numbersInText;
    private List<String> allNumbersInText;

    public parseNumbers() {
        numbersInText = new ArrayList<>();
        allNumbersInText = new ArrayList<>();
    }

    @Override
    public void parse(Document d)
    {

//        this.splitDocText(d);
        docText = d.getDocText().text().split(" ");

        int countNumberMatch=0,allNumbers=0;
        for (String word :
                docText) {
            if(word.matches("^\\d.*"))
            {
                if(word.charAt(word.length()-1)=='.')
                {
                    word = word.substring(0,word.length()-1);
                }
                allNumbers++;
                word = chopDownLastCharPunc(word);
                allNumbersInText.add(word);
                //System.out.println(word);

                if(word.matches("^(\\d+|\\d{1,3}(,\\d{3})*)(\\.\\d+)?$"))
                {
                    countNumberMatch++;
                    numbersInText.add(word);
//                    System.out.println(word);
                }
                //TODO: this if is related to טווחים וביטויים section
//                else if(word.matches("^\\d+-\\d+$"))
//                {
//                    countNumberMatch++;
////                    numbersInText.add(word.split("-")[0]);
////                    numbersInText.add(word.split("-")[1]);
//                    numbersInText.add(word);
//                }
                else if(word.matches("^\\d+/\\d+$"))
                {
                    countNumberMatch++;
                    numbersInText.add(word);
                }
            }
        }

        //System.out.println("\n\n\n"+countNumberMatch+"/"+allNumbers);


    }
}
