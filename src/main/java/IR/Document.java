package IR;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.util.HashMap;

/**
 * IR.Document Class is representing a document in the corpus
 * it has 7 parameters:
 * termsDictonary - HashMap that hold all the Terms in the Doc and how many times in it.
 * docText - The text of the IR.Document
 * docNo - The DocNumber of the IR.Document
 * docDate - The DocNumber of the IR.Document
 * textArray - The DocNumber of the IR.Document
 * maxTFTerm - The DocNumber of the IR.Document
 * firstInsert - The DocNumber of the IR.Document
 * @see Element
 * @see HashMap
 */
public class Document implements Serializable {


    private HashMap<String,Integer> termsDictonary;// Each term has an Integer that presets how many times this term is present in the IR.Document
    //private String docText;
    private String docNo;
    private String docDate;
    private String[] textArray;
    private String maxTFTerm;
    private int maxTF;
    private boolean firstInsert;
    private String[] headLine;

    public Document()
    {
        termsDictonary = new HashMap<>();
        docDate = "";
        docNo = "123";
        //docText = "gal123";
        maxTFTerm = "";
        maxTF = 0;
        firstInsert = true;
        textArray = null;
    }

    public Document(Element fileDocInCorpus) {
        this.termsDictonary = new HashMap<>();
        //docText = fileDocInCorpus.getElementsByTag("text").text();
        docNo = fileDocInCorpus.getElementsByTag("docno").text();
        docDate = fileDocInCorpus.getElementsByTag("date1").text();
        textArray = StringUtils.split(fileDocInCorpus.getElementsByTag("text").text()," =");
        maxTFTerm = "";
        maxTF = 0;
        firstInsert = true;
        if(docNo.substring(0,2).equals("FB")) {
            headLine = StringUtils.split(fileDocInCorpus.getElementsByTag("ti").text(), " ");
        }else if(docNo.substring(0,2).equals("FT") || docNo.substring(0,2).equals("LA")){
            headLine = StringUtils.split(fileDocInCorpus.getElementsByTag("headline").text(), " ");
        }
        else{
            headLine = null;
        }
    }

    public Document(String query,String id){
        this.termsDictonary = new HashMap<>();
        //docText = query;
        docNo = id;
        textArray = StringUtils.split(query," =");
        maxTFTerm = "";
        maxTF = 0;
        firstInsert = true;
    }

    public String getDocDate() {
        return docDate;
    }


    public String[] getHeadLine() {
        return headLine;
    }

    public String getDocNo() {

        return docNo;
    }

    public String[] getTextArray() {
        return textArray;
    }


    public String getMaxTfTerm()
    {
        return maxTFTerm;
    }

    public int getNumUniqeTerm()
    {
        return termsDictonary.size();
    }

    /**
     * if newTerm is not in the Doc termsDictionary it adds to the Dictionary with value 1
     * @param newTerm - a IR.Term that suppose to be in this Doc text docText
     */
    public void insertFoundTermInDoc(String newTerm)
    {
        if(newTerm != null )
        {
            if(!termsDictonary.containsKey(newTerm))
            {
                termsDictonary.put(newTerm,1);
            }
            else
            {
                increaseTermCountInDoc(newTerm);
            }
        }

        if(firstInsert)
        {
            maxTFTerm = newTerm;
            firstInsert = false;
            maxTF = 1;

        }
        else
        {
            if (termsDictonary.get(newTerm) > termsDictonary.get(maxTFTerm))
            {
                maxTFTerm = newTerm;
                maxTF = termsDictonary.get(maxTFTerm);
            }
        }
    }

//    public String getDocText() {
//        return docText;
//    }

    /**
     * if the existingTerm is already in the termsDictionary its value is increased by 1
     * @param existingTerm - a term that is already inside the dictionary
     */
    public void increaseTermCountInDoc(String existingTerm)
    {
        if(existingTerm != null)
        {
            if(termsDictonary.containsKey(existingTerm))
            {
                Integer oldCount = termsDictonary.get(existingTerm);
                Integer newCount = oldCount++;
                termsDictonary.replace(existingTerm,oldCount,newCount);
            }
        }
    }

    public int getMaxTF() {
        return maxTF;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;
        Document document = (Document) o;
        if(document.textArray.length == 0)
            return false;
        return this.textArray.length == document.textArray.length &&
                this.textArray[0].equals(document.textArray[0]) && this.getDocNo().equalsIgnoreCase(document.getDocNo());
    }
}
