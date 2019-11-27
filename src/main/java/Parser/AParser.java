package Parser;

import IR.Document;
import IR.Term;
import Tokenizer.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public abstract class AParser{

    protected char[] punctuations = {',','.',';',':','?','(',')','"','{','}'};
    protected String[] docText;
    protected Tokenizer toknizr = Tokenizer.getInstance();
    protected String stopWords;

    protected AParser()
    {
        stopWords = "";
        createStopWords();
    }

    /**
     * Creates a String that contains all the stopwords from the file <b>resources/stopWords.txt</b>
     */
    protected void createStopWords()
    {
        File stopWordsFile = new File("./src/main/resources/stopWords.txt");
        if(!stopWordsFile.exists())
        {
            System.out.println(stopWordsFile.getAbsolutePath());
        }

        try
        {
            BufferedReader stopWordsReader = new BufferedReader(new FileReader(stopWordsFile));

            String word = stopWordsReader.readLine();
            while(word != null)
            {
                stopWords += " "+word.toLowerCase();
                word = stopWordsReader.readLine();
            }

            stopWordsReader.close();
//            this.stopWords = (List<String>) Fileo.readObject();

//            Filer.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

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
        else
        {
            docText = null;
        }
    }


    protected String chopDownLastCharPunc(String word) {

        if(word != null && word.length() >= 1)
        {
//            word = word.toLowerCase();
            while(isLastCharPunctuation(word))
            {
                word = word.substring(0,word.length()-1);
            }
            return word;
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
