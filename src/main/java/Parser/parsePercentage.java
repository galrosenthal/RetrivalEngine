package Parser;

import IR.Document;
import IR.Term;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parsePercentage extends AParser {
    int count = 0;
    public static int numOfTerms = 0;
    Pattern pattern;
    Matcher matcher;

    public parsePercentage() {
        super();
        //pattern = Pattern.compile();
        //matcher = pattern.matcher(word.toLowerCase());
    }

    @Override
    public void parse(String[] wordsInDoc) {
        if(wordsInDoc != null) {
            int i = 0;
            char lsatChar;
            for (String word :
                    wordsInDoc) {
                word = chopDownFisrtChar(word);
                word = chopDownLastCharPunc(word);

                if(word != null) {

                    if (word.length() > 0 && word.substring(word.length() - 1).equals("%")) {
                    //if (word.length() > 0 && word.matches("\\b(?<!\\.)(?!0+(?:\\.0+)?%)(?:\\d|[1-9]\\d|100)(?:(?<!100)\\.\\d+)?%")) {
                        numOfTerms++;
                        //word = chopDownFisrtChar(word);
                       if (NumberUtils.isNumber(word.substring(0, word.length() - 1))) {
                            //double num = Double.parseDouble(word.substring(0, word.length() - 1));
                            Term newTerm = new Term(word);
                            numOfTerms++;
                            //System.out.println(newTerm.getWordValue());
                        } else if (isFraction(word.substring(0, word.length() - 1))) {
                            if (i > 2 && NumberUtils.isDigits(wordsInDoc[i - 1])) {
                                Term newTerm = new Term(wordsInDoc[i - 1] + " " + word);
                                numOfTerms++;
                                //System.out.println(newTerm.getWordValue());
                            } else {
                                Term newTerm = new Term(word);
                                numOfTerms++;
                                //System.out.println(newTerm.getWordValue());
                            }

                        }
                    } else if (word.equalsIgnoreCase("percentage") || word.equalsIgnoreCase("percent") ||
                            word.equalsIgnoreCase("percentages") || word.equalsIgnoreCase("percents")) {
                        if (i>0) {
                            String lastWord = chopDownLastCharPunc(wordsInDoc[i - 1]);
                            lastWord = chopDownFisrtChar(wordsInDoc[i - 1]);

                            if (NumberUtils.isNumber(lastWord)) {
                            Term newTerm = new Term(lastWord + "%");
                            count++;
                            numOfTerms++;
                            //System.out.println(newTerm.getWordValue());

                            } else if (isFraction(lastWord)) {
                                if (i > 2 && NumberUtils.isDigits(wordsInDoc[i - 2])) {
                                Term newTerm = new Term(wordsInDoc[i - 2] + " " + word);
                                numOfTerms++;
                                //System.out.println(newTerm.getWordValue());
                                } else {
                                Term newTerm = new Term(word);
                                numOfTerms++;
                                //System.out.println(newTerm.getWordValue());
                                }

                            }
                        }
                    }
                }

                i++;
            }
        }

    }

    public static int getNumOfTerms() {
        return numOfTerms;
    }
}
