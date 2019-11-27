package Parser;

import IR.Document;
import IR.Term;
import Tokenizer.Tokenizer;
import org.apache.commons.lang3.math.NumberUtils;

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
            if(word.length()> 0 && word.charAt(word.length()-1) == punc)
            {
                return true;
            }
        }
        return false;
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

    protected boolean isFraction(String word){
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
