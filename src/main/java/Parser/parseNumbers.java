package Parser;

import IR.Document;
import IR.Term;

public class parseNumbers extends AParser {

    private final double BILLION = 1000000000;
    private final double MILLION = 1000000;
    private final double THOUSAND = 1000;

    public parseNumbers() {
    }

    @Override
    public void parse(Document d) throws Exception {
        if(d == null)
            throw new Exception("The Document is Null");
        this.splitDocText(d);


        if(docText == null || docText.length == 0)
            throw new Exception("There is no text in this Document");

        int wordIndexInText = 0;
        for (String word :
                docText) {

            if(isLastCharPunctuation(word))
            {
                chopDownLastChar(word);
            }

            if(isFirstCharNumberAndNotZero(word))
            {
                Double wordIsNumber;
                if(isNumberSeperatedComma(word))
                {
                    word = word.replaceAll(",","");
                }
                try{
                    wordIsNumber = Double.valueOf(word);

                    Term numberTerm = createTermFromNumber(wordIsNumber);

                }
                catch (NumberFormatException e)
                {
                    continue;
                }
            }




            wordIndexInText++;
        }






    }

    private Term createTermFromNumber(Double wordIsNumber) {
        double numToSave;
        String termNum;
        if(wordIsNumber.compareTo(BILLION) >= 0)
        {
            numToSave = wordIsNumber/BILLION;
            termNum = numToSave + "B";
        }
        else if (wordIsNumber.compareTo(MILLION) >= 0)
        {
            numToSave = wordIsNumber/MILLION;
            termNum = numToSave + "M";
        }
        else if (wordIsNumber.compareTo(THOUSAND) >= 0)
        {
            numToSave = wordIsNumber/THOUSAND;
            termNum = numToSave + "K";
        }
        else
        {
            numToSave = wordIsNumber;
            termNum = numToSave + "";
        }
        Term numberTerm = new Term(termNum);
        return numberTerm;
    }

    private boolean isNumberSeperatedComma(String word) {
        if(word == null)
            return false;

        char[] numberChars = word.toCharArray();
        for (int charIndex = 0; charIndex < word.length() ; charIndex++) {
            if(numberChars[charIndex] == ',')
                return true;
        }

        return false;
    }

    private boolean isFirstCharNumberAndNotZero(String word) {
        char[] number19 = {'1','2','3','4','5','6','7','8','9'};

        for (char num:
                number19) {
            if(word.charAt(0) == num)
            {
                return true;
            }
        }
        return false;
    }
}
