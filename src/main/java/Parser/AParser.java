package Parser;

import IR.Document;
import IR.Term;
import Tokenizer.Tokenizer;

public abstract class AParser{

    protected char[] punctuations = {',','.',';',':','?'};
    protected String[] docText;
    protected Tokenizer toknizr = Tokenizer.getInstance();
    public abstract void parse(Document d) throws Exception;



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
        else
        {
            docText = null;
        }
    }


    protected String chopDownLastChar(String word) {

        if(word != null && word.length() >= 1)
        {
            word = word.toLowerCase();
            if(isLastCharPunctuation(word))
            {
                word = word.substring(0,word.length()-1);
                return word;
            }
        }
        return null;
    }

    protected boolean isLastCharPunctuation(String word) {
        if(word == null)
        {
            return false;
        }

        for (char punc :
                punctuations) {
            if(word.charAt(word.length()-1) == punc)
            {
                return true;
            }
        }
        return false;
    }


}
