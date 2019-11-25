package Parser;

import IR.Document;
import IR.Term;
import Tokenizer.Tokenizer;
import org.apache.commons.lang3.math.NumberUtils;

public abstract class AParser{

    protected String[] docText;
    protected Tokenizer toknizr = Tokenizer.getInstance();
    public abstract void parse(Document d);



    protected boolean checkTermExist(Term term)
    {
        return toknizr.getTokenList().containsKey(term);
    }


    protected void splitDocText(Document d)
    {
        if (d != null)
        {
           docText = d.getDocText().text().split(" ");
        }

        docText = null;
//        return wordsInDoc;
    }


    protected String chopDownLastChar(String word) {
        char[] punctuations = {',','.',';',':','?','|'};
        if(word != null && word.length() >= 2)
        {
            word = word.toLowerCase();
            for (char punc :
                    punctuations) {
                if(word.charAt(word.length()-1) == punc)
                {
                    word = word.substring(0,word.length()-1);
                    break;
                }
            }
            return word;
        }
        return null;
    }

    protected String chopDownFisrtChar(String word) {
        char[] punctuations = {',','.',';',':','?','|','('};

        if(word != null && word.length() >= 2)
        {
            word = word.toLowerCase();
            for (char punc :
                    punctuations) {
                if(word.charAt(0) == punc)
                {
                    word = word.substring(1);
                    break;
                }
            }
        }
        return word;
    }

    protected boolean checkIfFraction(String word){
        boolean isFraction = false;

        if(NumberUtils.isNumber(word.substring(0,0)) && NumberUtils.isNumber(word.substring(2,2)) &&
                word.substring(2,2).equals("/")){
            isFraction = true;
        }

        return isFraction;
    }

    protected double fractionToDecimal(String word){
        double num1 = Double.parseDouble(word.substring(0,0));
        double num2 = Double.parseDouble(word.substring(1,1));
        double fraction = num1/num2;
        double fractionValue = (double) (fraction * 10);
        double decimal = fractionValue % 10;
        double value = decimal * 0.1;
        return value;
    }

}
