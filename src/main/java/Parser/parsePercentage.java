package Parser;

import IR.Document;
import IR.Term;
import org.apache.commons.lang3.math.NumberUtils;

public class parsePercentage extends AParser {
    int count = 0;

    public parsePercentage() {
        super();
    }

    @Override
    public void parse(Document d) {
        if(d != null) {
            int i = 0;
            char lsatChar;
            String[] wordsInDoc = d.getDocText().text().split(" ");
            for (String word :
                    wordsInDoc) {
                word = chopDownFisrtChar(word);
                word = chopDownLastCharPunc(word);

                if(word != null) {

                    if (word.length() > 0 && word.substring(word.length() - 1).equals("%")) {
                        word = chopDownFisrtChar(word);
                        if (NumberUtils.isNumber(word.substring(0, word.length() - 1))) {
                            //double num = Double.parseDouble(word.substring(0, word.length() - 1));
                            Term newTerm = new Term(word);
                            //System.out.println(newTerm.getWordValue());
                        } else if (isFraction(word.substring(0, word.length() - 1))) {
                            if (i > 2 && NumberUtils.isDigits(wordsInDoc[i - 1])) {
                                Term newTerm = new Term(wordsInDoc[i - 1] + " " + word);
                                //System.out.println(newTerm.getWordValue());
                            } else {
                                Term newTerm = new Term(word);
                                //System.out.println(newTerm.getWordValue());
                            }

                        }
                    } else if (word.equals("percentage") || word.equals("percent")) {
                        String lastWord = chopDownLastCharPunc(wordsInDoc[i - 1]);
                        lastWord = chopDownFisrtChar(wordsInDoc[i - 1]);
                        if (NumberUtils.isNumber(lastWord)) {
                            Term newTerm = new Term(lastWord + "%");
                            count++;
                            //System.out.println(newTerm.getWordValue());

                        } else if (isFraction(lastWord)) {
                            if (i > 2 && NumberUtils.isDigits(wordsInDoc[i - 2])) {
                                Term newTerm = new Term(wordsInDoc[i - 2] + " " + word);
                                //System.out.println(newTerm.getWordValue());
                            } else {
                                Term newTerm = new Term(word);
                                //System.out.println(newTerm.getWordValue());
                            }
                        }
                    }
                }

                i++;
            }
        }

    }
}
