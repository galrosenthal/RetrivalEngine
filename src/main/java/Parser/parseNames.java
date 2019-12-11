package Parser;

import IR.Document;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseNames extends AParser {
    String[] splitedText;
    String pattern = "([A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*)|([A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*)|([A-Z]+[a-z]*\\s[A-Z]+[a-z]*)|([A-Z]+[a-z]*)";
    Pattern p = Pattern.compile(pattern);
    Matcher matcher;
    StringBuilder sentence = new StringBuilder("");
    StringBuilder wordB;
    int i=0;
    int numOfWords=0;


    @Override
    public void parse() {
    }
//
//        while(!queueIsEmpty()) {
//            i=0;
//            Document document = dequeueDoc();
//
//            splitedText = document.getDocText().text().split(" ");
//
//            //splitedText = document.getDocText().text().split("[\\s?!();\":\\n\\t/*]");
//
//            for (String word : splitedText) {
//                /*
//                if(i< splitedText.length-4){
//                    fourSentece = splitedText[i] +" "+ splitedText[i+1]+" "+splitedText[i+2] +" "+ splitedText[i+3];
//                }
//                else if(i< splitedText.length-3){
//                    thirdSentece = splitedText[i] +" "+ splitedText[i+1]+" "+splitedText[i+2];
//                }
//                else if(i< splitedText.length-2){
//                    secondSentece = splitedText[i] +" "+ splitedText[i+1]+" "+splitedText[i+2];
//                }
//                //matcher = p.matcher(sentece);
//
//                if(fourSentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
//                    //System.out.println(fourSentece);
//                    parsedTermInsert(fourSentece,document.getDocNo());
//                }
//                 else if(thirdSentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
//                    //System.out.println(thirdSentece);
//                    parsedTermInsert(thirdSentece,document.getDocNo());
//
//                }
//                 else if(thirdSentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
//                    //System.out.println(thirdSentece);
//                    parsedTermInsert(secondSentece,document.getDocNo());
//                }
//
//                 if(word.matches("[A-Z]+[a-z]*")){
//                    parsedTermInsert(word,document.getDocNo());
//                }
//                 //parsedTermInsert(word,document.getDocNo());
//                       sentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*") ||
//                        sentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
//                    parsedTermInsert(sentece,document.getDocNo());
//
//                        for (String term :terms) {
//                            parsedTermInsert(term,document.getDocNo());
//                        }
//                    }
//                }*/
//                if ((!word.equals("")) && Character.isUpperCase(word.charAt(0))) {
//                    if (word.charAt(word.length() - 1) == '.'
//                            || word.charAt(word.length() - 1) == '"' || word.charAt(word.length() - 1) == ',' ||
//                            word.charAt(word.length() - 1) == ';' || word.charAt(word.length() - 1) == ':') {
//                        sentence.append(word.substring(0, word.length() - 1));
//
//                        String[] sentenceLengh = sentence.toString().split(" ");
//                        if(sentenceLengh.length > 1){
//                            //System.out.println(sentence);
//                            parsedTermInsert(sentence.substring(0, sentence.length() - 1), document.getDocNo());
//                        }
//                        sentence.setLength(0);
//
//                    } else {
//                        sentence.append(word + " ");
//                    }
//                } else if (!sentence.equals("")) {
//
//                    String[] sentenceLengh = sentence.toString().split(" ");
//                    if(sentenceLengh.length > 1){
//                        //System.out.println(sentence);
//                        parsedTermInsert(sentence.substring(0, sentence.length() - 1), document.getDocNo());
//                    }
//                    sentence.setLength(0);
//                }
//                i++;
//            }
//            this.releaseToIndexerFile();
//        }
//    }

    @Override
    public void run() {
        System.out.println("Names Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Names Numbers is stopped");
    }

    public void parseNoThread(Document document){
        //splitedText = StringUtils.split(document.getDocText().text()," ");
        //splitedText = document.getDocText().text().split(" ");
        splitedText = document.getDocText().text().split("[\\s?!();\":\n\r\\t/*]");
        numOfWords=0;
        for (String word : splitedText) {

            wordB = new StringBuilder(word);
            //wordB = chopDownLastCharPunc(wordB);
            if ((wordB.length()>0) && Character.isUpperCase(wordB.charAt(0))) {
                if (wordB.charAt(wordB.length() - 1) == '.'
                        || wordB.charAt(wordB.length() - 1) == '"' || wordB.charAt(wordB.length() - 1) == ',' ||
                        word.charAt(wordB.length() - 1) == ';' || wordB.charAt(wordB.length() - 1) == ':') {
                    sentence.append(wordB.substring(0, word.length() - 1));

                    //String[] sentenceLengh = sentence.toString().split(" ");
                    if(numOfWords > 1){
                        //System.out.println(sentence);
                        parsedTermInsert(sentence.substring(0, sentence.length() - 1), document.getDocNo());
                    }
                    numOfWords=0;
                    sentence.setLength(0);

                } else {
                    numOfWords++;
                    sentence.append(word).append(" ");
                }
            } else if (numOfWords > 1) {
                //String[] sentenceLengh = sentence.toString().split(" ");

                    //System.out.println(sentence);
                    parsedTermInsert(sentence.substring(0, sentence.length() - 1), document.getDocNo());
                numOfWords=0;
                sentence.setLength(0);
            }
            i++;
        }
        this.releaseToIndexerFile();
    }
}
