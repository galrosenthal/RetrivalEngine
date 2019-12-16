//package Parser;
//
//import IR.Document;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.math.NumberUtils;
//
//public class parseWords extends AParser {
//    String[] splitedText;
//
//    public parseWords() {
//        super();
//    }
//
//    @Override
//    public void parse() {
//        while(!isQEmpty()) {
//            Document document = dequeueDoc();
//
//
//            splitedText = StringUtils.split(document.getDocText().text(),' ');
//
//            for (String word : splitedText) {
//                word = chopDownFisrtChar(word);
//                word = chopDownLastCharPunc(word);
//                if (stopWords.contains(word.toLowerCase()) || NumberUtils.isNumber(word) || word.equals("") ) {
//                    continue;
//                }
//                else{
//                    //System.out.println(word);
//                    parsedTermInsert(word,document.getDocNo());
//                }
//                /*
//                if(Character.isUpperCase(word.charAt(0))){
//                    if(this.termsInText.containsKey(word.toLowerCase())){
//                        parsedTermInsert(word.toLowerCase(),document.getDocNo());
//                    }
//                }
//                else if(word.equals(word.toLowerCase())){
//
//                }*/
//            }
//        }
//    }
//
//    public void parse(Document document){
//
//        splitedText = document.getDocText().text().split(" ");
//        for (String word : splitedText) {
//
//            StringBuilder wordB = new StringBuilder(word);
//            wordB = chopDownFisrtChar(wordB);
//            wordB = chopDownLastCharPunc(wordB);
//            if (stopMWords.contains(wordB.toString().toLowerCase()) || wordB.toString().equals("") ) {
//                continue;
//            }
//            //else if (wordB.toString().chars().allMatch(Character::isLetter)){
//            else if (bettertWay(wordB.toString())){
//                //System.out.println(word);
//                parsedTermInsert(word,document.getDocNo());
//            }
//                /*
//                if(Character.isUpperCase(word.charAt(0))){
//                    if(this.termsInText.containsKey(word.toLowerCase())){
//                        parsedTermInsert(word.toLowerCase(),document.getDocNo());
//                    }
//                }
//                else if(word.equals(word.toLowerCase())){
//
//                }*/
//        }
//    }
//
//    public static boolean bettertWay(String name) {
//        char[] chars = name.toCharArray();
//        long startTimeOne = System.nanoTime();
//        for(char c : chars){
//            if(!(c>=65 && c<=90)&&!(c>=97 && c<=122) ){
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public boolean isAlpha(String name) {
//        char[] chars = name.toCharArray();
//
//        for (char c : chars) {
//            if(!Character.isLetter(c)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    @Override
//    public void run() {
//        System.out.println("Words Parser has started");
//        while(!stopThread)
//        {
//            parse();
//        }
//        System.out.println("Words Numbers is stopped");
//
//    }
//}
