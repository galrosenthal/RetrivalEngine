package Tokenizer;

import IR.Term;

import java.util.HashMap;
import java.util.Map;

/**
 * Public Singelton To create a tokenized list of all the words in the corpus
 */
public class Tokenizer {


    private static volatile Tokenizer mInstance;
    private Map<Term, String> tokenList; // Term -> Path of posting file
    //    private Map<String, String> tokenList;
    private Tokenizer() {

//        tokenList = new HashMap<>();
        tokenList = new HashMap<Term,String>();
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

    public Map<Term, String> getTokenList() {
        return tokenList;
    }



//    /**
//     * This Functino Creates Token list from text of a document in the corpus
//     * @param docNo - the Number of the IR.Document in the corpus
//     * @param docText - the Text of the IR.Document
//     * @return - A token map of Word->{Documents List} where DocumentList is all Documents number that the word is shown in,
//     * seperated by ;.
//     */
//    public Map<String,ArrayList<String>> tokenizingText(Elements docNo, Elements docText)
//    {
//        /**
//         * Split the text into seperate words by " "
//         * maybe it shoud be in a seperate function so we could seperate by different chars
//         */
//        String[] wordsInDoc = docText.text().split(" ");
//        ArrayList<String> docsList = null;
//        for (String word :
//                wordsInDoc) {
//            word = chopDownLastChar(word);
//
//
////            tokenList.put(word,docNo.text());
//            if(!tokenList.containsKey(word))
//            {
//                docsList = new ArrayList<>();
//                docsList.add(docNo.text());
//                tokenList.put(word,docsList);
//            }
//            else
//            {
//                if(!tokenList.get(word).contains(docNo.text()))
//                {
//                    tokenList.get(word).add(docNo.text());
//
//                }
//
//
//
////                tokenList.put(word,);
//
//            }
//
//        }
//
//        return null;
//    }


    private String chopDownLastChar(String word) {
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
