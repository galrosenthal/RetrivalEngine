package Parser;

import IR.Document;

import java.util.HashMap;

public class parseNumbers extends AParser {

    private final double BILLION = 1000000000;
    private final double MILLION = 1000000;
    private final double THOUSAND = 1000;
//    private List<String> numbersInText;
    private HashMap<String,Integer> numbersInText;
//    private List<String> allNumbersInText;
    private HashMap<String,Integer> allNumbersInText;

    public parseNumbers() {
        super();
        numbersInText = new HashMap<>();
        allNumbersInText = new HashMap<>();
    }

    @Override
    public void parse(String[] wordsInDoc)
    {

//        this.splitDocText(d);
        docText = wordsInDoc;

        int countNumberMatch=0,allNumbers=0;
        for (int wordIndex = 0; wordIndex < docText.length; wordIndex++) {
            String word = docText[wordIndex];
            if(stopWords.contains(word.toLowerCase()))
            {
                continue;
            }
            if(word.matches("^\\d.*")) {
                if (word.charAt(word.length() - 1) == '.') {
                    word = word.substring(0, word.length() - 1);
                }
                if (word.matches("^\\d+-\\d+$"))
                {
                    continue;
                }
                allNumbers++;
                word = chopDownLastCharPunc(word);
//                if(allNumbersInText.containsKey(word))
//                {
//                    allNumbersInText.put(word,allNumbersInText.get(word)+1);
//                }
//                else
//                {
//                    allNumbersInText.put(word,1);
//                }
                //System.out.println(word);

                if(word.matches("^(\\d+|\\d{1,3}(,\\d{3})*)(\\.\\d+)?$"))
                {
                    countNumberMatch++;
                    if(numbersInText.containsKey(word))
                    {
                        numbersInText.put(word,numbersInText.get(word)+1);
                    }
                    else
                    {
                        numbersInText.put(word,1);
                    }
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
                    if(numbersInText.containsKey(word))
                    {
                        numbersInText.put(word,numbersInText.get(word)+1);
                    }
                    else
                    {
                        numbersInText.put(word,1);
                    }
                }
            }
        }

        //System.out.println("\n\n\n"+countNumberMatch+"/"+allNumbers);


    }

    public HashMap<String, Integer> getNumbersInText() {
        return numbersInText;
    }
}
