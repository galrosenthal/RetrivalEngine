package Parser;

import IR.Document;
import IR.Term;
import Tokenizer.Tokenizer;

import java.util.List;

public abstract class AParser <E>{

    private Tokenizer toknizr = Tokenizer.getInstance();
    public abstract List<E> parse(Document d);



    protected boolean checkTermExist(Term term)
    {
        return toknizr.getTokenList().containsKey(term);
    }



}
