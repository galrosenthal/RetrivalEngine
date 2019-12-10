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
    int i=0;

    @Override
    public void parse() {
        String sentece = "";
        while(!queueIsEmpty()) {
            i=0;
            Document document = dequeueDoc();


            splitedText = document.getDocText().text().split("[\\s?!();\":]");

            for (String word : splitedText) {
                /*
                if(i< splitedText.length-4){
                    fourSentece = splitedText[i] +" "+ splitedText[i+1]+" "+splitedText[i+2] +" "+ splitedText[i+3];
                }
                else if(i< splitedText.length-3){
                    thirdSentece = splitedText[i] +" "+ splitedText[i+1]+" "+splitedText[i+2];
                }
                else if(i< splitedText.length-2){
                    secondSentece = splitedText[i] +" "+ splitedText[i+1]+" "+splitedText[i+2];
                }
                //matcher = p.matcher(sentece);

                if(fourSentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
                    //System.out.println(fourSentece);
                    parsedTermInsert(fourSentece,document.getDocNo());
                }
                 else if(thirdSentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
                    //System.out.println(thirdSentece);
                    parsedTermInsert(thirdSentece,document.getDocNo());

                }
                 else if(thirdSentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
                    //System.out.println(thirdSentece);
                    parsedTermInsert(secondSentece,document.getDocNo());
                }

                 if(word.matches("[A-Z]+[a-z]*")){
                    parsedTermInsert(word,document.getDocNo());
                }
                 //parsedTermInsert(word,document.getDocNo());
                       sentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*\\s[A-Z]+[a-z]*") ||
                        sentece.matches("[A-Z]+[a-z]*\\s[A-Z]+[a-z]*")){
                    parsedTermInsert(sentece,document.getDocNo());

                        for (String term :terms) {
                            parsedTermInsert(term,document.getDocNo());
                        }
                    }
                }*/
                if((!word.equals("")) && Character.isUpperCase(word.charAt(0)) && word.charAt(word.length()-1) !='.'
                        && word.charAt(word.length()-1) !=','){
                    sentece += word + " ";
                    parsedTermInsert(word,document.getDocNo());
                }
                else if(!sentece.equals("")){
                   // System.out.println(sentece.substring(0,sentece.length()-1));
                    parsedTermInsert(sentece.substring(0,sentece.length()-1),document.getDocNo());
                    sentece = "";
                }
                i++;
            }
            numOfParsedDocInIterative++;
            //releaseToIndexerFile();
        }
    }

    @Override
    public void run() {
        System.out.println("Names Parser has started");
        while(!stopThread)
        {
            parse();
        }
        System.out.println("Names Numbers is stopped");
    }
}
