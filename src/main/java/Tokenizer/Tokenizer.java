package Tokenizer;

import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Public Singelton To create a tokenized list of all the words in the corpus
 */
public class Tokenizer {


    private static volatile Tokenizer mInstance;
    private Map<String, ArrayList<String>> tokenList;
//    private Map<String, String> tokenList;
    private Tokenizer() {

//        tokenList = new HashMap<>();
        tokenList = new HashMap<String, ArrayList<String>>();
    }
    public static Tokenizer getInstance() {
        if (mInstance == null) {
            synchronized (Tokenizer.class) {
                if (mInstance == null) {
                    mInstance = new Tokenizer();
                }
            }
        }
        return mInstance;
    }

    private Map<String, ArrayList<String>> getTokenList() {
        return tokenList;
    }
    private void setTokenList(Map<String, ArrayList<String>> tokenList) {
        this.tokenList = tokenList;
    }

    /**
     * This Functino Creates Token list from text of a document in the corpus
     * @param docNo - the Number of the Document in the corpus
     * @param docText - the Text of the Document
     * @return - A token map of Word->{Documents List} where DocumentList is all Documents number that the word is shown in,
     * seperated by ;.
     */
    public Map<String,ArrayList<String>> tokenizingText(Elements docNo, Elements docText)
    {
        /**
         * Split the text into seperate words by " "
         * maybe it shoud be in a seperate function so we could seperate by different chars
         */
        String[] wordsInDoc = docText.text().split(" ");
        ArrayList<String> docsList = null;
        for (String word :
                wordsInDoc) {


//            tokenList.put(word,docNo.text());
            if(!tokenList.containsKey(word))
            {
                docsList = new ArrayList<>();
                docsList.add(docNo.text());
                tokenList.put(word,docsList);
            }
            else
            {
                if(!tokenList.get(word).contains(docNo.text()))
                {
                    tokenList.get(word).add(docNo.text());

                }



//                tokenList.put(word,);

            }

        }

        return null;
    }

//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Tokenizer)) return false;
//        Tokenizer tokenizer = (Tokenizer) o;
//        return getTokenList().equals(tokenizer.getTokenList());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getTokenList());
//    }
}
