package Parser;//package Parser;
//
//import IR.Document;
//import IR.Term;
//
//public class parseNumbers extends AParser {
//
//    private final double BILLION = 1000000000;
//    private final double MILLION = 1000000;
//    private final double THOUSAND = 1000;
//
//    public parseNumbers() {
//    }
//
//    @Override
//    public void parse(Document d) throws Exception {
//        if(d == null)
//            throw new Exception("The Document is Null");
//        this.splitDocText(d);
//
//
//        if(docText == null || docText.length == 0)
//            throw new Exception("There is no text in this Document");
//
//        for (int wordIndexInText = 0; wordIndexInText < docText.length;)
//        {
//
//            String word = docText[wordIndexInText];
//            Double wordIsNumber;
//            word = chopDownLastCharPunc(word);
//            // Check if the next word is relevant
//            if(wordIndexInText < docText.length && isNextWordRelevantToNumber(docText[wordIndexInText+1]))
//            {
//
//                wordIsNumber = checkNumberReleveance(word);
//                if(wordIsNumber == 0)
//                {
//                    continue;
//                }
//                Term numberAndQuntify = createTermFromNumberAndQuntify(wordIsNumber,docText[wordIndexInText+1]);
//            }
//            else
//            {
//                wordIsNumber = checkNumberReleveance(word);
//                if(wordIsNumber == 0)
//                {
//                    continue;
//                }
//                Term numberTerm = createTermFromNumber(wordIsNumber);
//            }
//
//
//
//
//
//
//            wordIndexInText++;
//        }
//
//
//
//
//
//
//    }
//
//    private Term createTermFromNumberAndQuntify(Double wordIsNumber, String quntify) {
//        //Create Term from 2 strings
//        return null;
//    }
//
//
//    /**
//     * Checks whether or not the number is a number or not
//     * @param word - string that is number
//     * @return 0 if the the string is not a valid number and the number itself if it is valid
//     */
//    private double checkNumberReleveance(String word)
//    {
//        double wordIsNumber = 0 ;
//        if(isFirstCharNumberAndNotZeroOrNegative(word))
//        {
//            if(isNumberSeperatedComma(word))
//            {
//                word = word.replaceAll(",","");
//            }
//            try
//            {
//                wordIsNumber = Double.parseDouble(word);
//            }
//            catch (NumberFormatException e)
//            {
//            }
//        }
//
//        return wordIsNumber;
//    }
//
//    /**
//     * Check if the quntify is a word representing a size (B,M,T)
//     * or if it is a fraction
//     * @return true if the string is a size(B,M,T) or if it is a fraction else returns false
//     */
//    private boolean isNextWordRelevantToNumber(String quntify) {
//        quntify = chopDownLastCharPunc(quntify);
//        if(quntify.equalsIgnoreCase("thousand") || quntify.equalsIgnoreCase("million") || quntify.equalsIgnoreCase("billion"))
//        {
//            return true;
//        }
//        else if(quntify.contains("/"))
//        {
//            if(quntify.matches("^\\d\\/\\d$");
//            {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Creates a new Term from the word in the text
//     *
//     * @param wordIsNumber number to create for the term
//     * @return a term from the number
//     */
//    private Term createTermFromNumber(Double wordIsNumber) {
//        double numToSave;
//        String termNum;
//        if(wordIsNumber.compareTo(BILLION) >= 0)
//        {
//            numToSave = wordIsNumber/BILLION;
//            termNum = numToSave + "B";
//        }
//        else if (wordIsNumber.compareTo(MILLION) >= 0)
//        {
//            numToSave = wordIsNumber/MILLION;
//            termNum = numToSave + "M";
//        }
//        else if (wordIsNumber.compareTo(THOUSAND) >= 0)
//        {
//            numToSave = wordIsNumber/THOUSAND;
//            termNum = numToSave + "K";
//        }
//        else
//        {
//            numToSave = wordIsNumber;
//            termNum = numToSave + "";
//        }
//        Term numberTerm = new Term(termNum);
//        return numberTerm;
//    }
//
//    /**
//     * Check if there are commas in the word,
//     * @return true if there are commas in the word
//     */
//    private boolean isNumberSeperatedComma(String word) {
//        if(word == null)
//            return false;
//
//        char[] numberChars = word.toCharArray();
//        for (int charIndex = 0; charIndex < word.length() ; charIndex++) {
//            if(numberChars[charIndex] == ',')
//                return true;
//        }
//
//        return false;
//    }
//
//    /**
//     * Check if the first char is number not 0 or if it is a negative number
//     * @return true if it is
//     */
//    private boolean isFirstCharNumberAndNotZeroOrNegative(String word) {
//        char[] number19 = {'1','2','3','4','5','6','7','8','9'};
//
//        if(word.charAt(0) == '-')
//            return true;
//
//        for (char num:
//                number19) {
//            if(word.charAt(0) == num)
//            {
//                return true;
//            }
//        }
//        return false;
//    }
//}
