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

    @Override
    public void run() {
        System.out.println("Precentage Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Precentage Parser has stopped");

    }

    public parsePercentage() {
        super();
        parseName = "PrecentageParser";
        //pattern = Pattern.compile();
        //matcher = pattern.matcher(word.toLowerCase());
    }

    @Override
    public void parse() {
        while (!queueIsEmpty()) {
            Document d = dequeueDoc();
            if (d == null) {
                continue;
            }

            if (d != null) {
                int i = 0;
                char lsatChar;
                String[] wordsInDoc = d.getDocText().text().split(" ");
                for (String word :
                        wordsInDoc) {
                    word = chopDownFisrtChar(word);
                    word = chopDownLastCharPunc(word);

                    if (word != null) {

                        if (word.length() > 0 && word.substring(word.length() - 1).equals("%")) {
                            //if (word.length() > 0 && word.matches("\\b(?<!\\.)(?!0+(?:\\.0+)?%)(?:\\d|[1-9]\\d|100)(?:(?<!100)\\.\\d+)?%")) {
                            numOfTerms++;
                            //word = chopDownFisrtChar(word);
                            if ((word.substring(0, word.length() - 1)).matches("^\\d+(\\.\\d+)?")) {
                                //double num = Double.parseDouble(word.substring(0, word.length() - 1));
                                parsedTermInsert(word,d.getDocNo());
                                //Term newTerm = new Term(word);
                                numOfTerms++;
                                System.out.println(word);
                            } else if (isFraction(word.substring(0, word.length() - 1))) {
                                if (i > 2 && wordsInDoc[i - 1].matches("^\\d+(\\.\\d+)?")) {
                                    parsedTermInsert(wordsInDoc[i - 1] + " " + word,d.getDocNo());
                                    //Term newTerm = new Term(wordsInDoc[i - 1] + " " + word);
                                    numOfTerms++;
                                    System.out.println(wordsInDoc[i - 1] + " " + word);
                                } else {
                                    parsedTermInsert(word,d.getDocNo());
                                    //Term newTerm = new Term(word);
                                    numOfTerms++;
                                    System.out.println(word);
                                }
                            }
                        } else if (word.equalsIgnoreCase("percentage") || word.equalsIgnoreCase("percent") ||
                                word.equalsIgnoreCase("percentages") || word.equalsIgnoreCase("percents")) {
                            if (i > 0) {
                                String lastWord = chopDownLastCharPunc(wordsInDoc[i - 1]);
                                lastWord = chopDownFisrtChar(wordsInDoc[i - 1]);

                                if (NumberUtils.isNumber(lastWord)) {
                                    parsedTermInsert(lastWord + "%",d.getDocNo());
                                    //Term newTerm = new Term(lastWord + "%");
                                    count++;
                                    numOfTerms++;
                                    System.out.println(lastWord + "%");

                                } else if (isFraction(lastWord)) {
                                    if (i > 2 && NumberUtils.isDigits(wordsInDoc[i - 2])) {
                                        parsedTermInsert(wordsInDoc[i - 2] + " " + word,d.getDocNo());
                                        //Term newTerm = new Term(wordsInDoc[i - 2] + " " + word);
                                        numOfTerms++;
                                        //System.out.println(newTerm.getWordValue());
                                    } else {
                                        parsedTermInsert(word,d.getDocNo());
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
            this.numOfParsedDocInIterative++;
            //this.releaseToIndexerFile();

        }
    }
    public static int getNumOfTerms() {
        return numOfTerms;
    }


}
