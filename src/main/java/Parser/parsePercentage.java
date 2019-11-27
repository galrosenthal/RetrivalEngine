package Parser;

import IR.Document;
import IR.Term;
import org.apache.commons.lang3.math.NumberUtils;

public class parsePercentage extends AParser {
    int count = 0;

    public parsePercentage() {
        super();
    }

    @Override
    public void parse(Document d) {
        if(d != null) {
            int i = 0;
            char lsatChar;
            String[] wordsInDoc = d.getDocText().text().split(" ");
            for (String word :
                    wordsInDoc) {
                chopDownLastCharPunc(word);
                String newWord = word.substring(word.length()-1);
                if(word.length() >0 && newWord.equals("%")){
                    word = chopDownFisrtChar(word);
                    if(NumberUtils.isNumber(word.substring(0,word.length()-1))){
                        //double num = Double.parseDouble(word.substring(0, word.length() - 1));
                        Term newTerm = new Term(word);
                        //System.out.println(newTerm.getWordValue());
                    }
                    else if(isFraction(newWord)){
                        Term newTerm = new Term(word);
                        System.out.println(newTerm.getWordValue());
                    }
                }
                else if(word.equals("percentage") || word.equals("percent")){

                    if(NumberUtils.isNumber(wordsInDoc[i-1])){
                        Term newTerm = new Term(wordsInDoc[i-1]+"%");
                        count++;
                        System.out.println(newTerm.getWordValue());

                    }
                    else if(isFraction(newWord)){
                        Term newTerm = new Term(word);
                        System.out.println(newTerm.getWordValue());
                    }
                }

                i++;
            }
        }

    }
}
