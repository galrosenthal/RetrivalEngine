package Parser;

import IR.Document;
import IR.Term;
import Tokenizer.Tokenizer;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public abstract class AParser{

    protected char[] punctuations = {',','.',';',':','?','(',')','"','{','}'};
    protected String[] docText;
    protected Tokenizer toknizr = Tokenizer.getInstance();
    protected String stopWords;
    protected HashMap<String,String> termsInText;


    protected AParser()
    {
        stopWords = "";
        termsInText = new HashMap<>();
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

        }
        return word;
    }

    protected boolean isLastCharPunctuation(String word) {
        if(word == null||word.length() == 0)
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
            while(isFirstCharPunctuation(word)){
                word = word.substring(1);
            }
        }
        return word;
    }

    protected  boolean isFirstCharPunctuation(String word) {
        if(word != null && word.length() >= 2)
        {
            word = word.toLowerCase();
            for (char punc :
                    punctuations) {
                if(word.charAt(0) == punc)
                {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isFraction(String word){
        boolean isFraction = false;

        if(word.length()> 2 && NumberUtils.isNumber(Character.toString(word.charAt(0))) && NumberUtils.isNumber(Character.toString(word.charAt(2))) &&
                Character.toString(word.charAt(1)).equals("/")){
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

    public void clearDic() {
        this.termsInText.clear();
    }

    /**
     * Gets a parsed number and inserting it to the Dictionary
     * @param term
     */
    protected void parsedTermInsert(String term, String currentDocNo) {
        //TODO: Change value of the Hashmap to String,String (word,docNo and other stuff)
        if (termsInText.containsKey(term)) {
            //TODO: check if the stored doc is the same as current doc, if yes increase count else create another doc string
//            int tf = Integer.parseInt(numbersInText.get(parsedNum).split(",")[1]);
            String docList = termsInText.get(term);
            String[] docsSplitted =  docList.split(";");
            boolean docAlreadyParsed = false;
            int oldtf = 0;
            String lastDocList = "";

            for (String docParams:
                 docsSplitted) {
                String[] docAndtf = docParams.split(",");
                oldtf = Integer.parseInt(docAndtf[1]);
                if(docAndtf[0].equals(currentDocNo))
                {
                    oldtf += 1;
                    docAlreadyParsed = true;
                }
                lastDocList += docAndtf + "," + oldtf + ";";
            }
            if(docAlreadyParsed)
            {
                lastDocList += currentDocNo + ",1;";
            }
            lastDocList = lastDocList.substring(0,lastDocList.length()-1);

            termsInText.replace(term,docList,lastDocList);



        } else {
            termsInText.put(term, currentDocNo+",1;");
        }
    }
}
