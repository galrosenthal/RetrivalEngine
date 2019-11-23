package Parser;

import IR.Document;
import IR.Term;
import Tokenizer.Tokenizer;

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
        char[] punctuations = {',','.',';',':','?'};
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


}
