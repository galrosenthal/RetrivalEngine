package IR;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * IR.DocumentInfo Class is representing the Info of a document in the corpus
 * it has 5 parameters:
 * docDate - The Date Field of this Document
 * namUniqueTerms - Each doc has many terms in it, this is the number of the Unique ones.
 * maxTfTerm - The Term with the max TF in the specific document
 * docNo - The Doc Number recorded in the corpus
 */
public class DocumentInfo implements Serializable
{


    private String docDate;
    private int numUniqueTerms;
    private String maxTfTerm;
    private int maxTfOfTerm;
    private String docNo;
    private HashMap<String,Integer> allEntitysInDoc;
    private int docLength;
    private String[] docTitle;


    public DocumentInfo(Document doc) {
        this.docNo = doc.getDocNo();
        this.maxTfTerm = doc.getMaxTfTerm();
        this.numUniqueTerms = doc.getNumUniqeTerm();
        this.docDate = doc.getDocDate();
        this.maxTfOfTerm = doc.getMaxTF();
        allEntitysInDoc = new HashMap<>();
        docLength = doc.getTextArray().length;
        docTitle = doc.getHeadLine();// can be null
    }

    public int getMaxTfOfTerm() {
        return maxTfOfTerm;
    }

    public String getDocDate() {
        return docDate;
    }

    public int getNumUniqueTerms() {
        return numUniqueTerms;
    }

    public int getDocLength(){
        return docLength;
    }

    public String getMaxTfTerm() {
        return maxTfTerm;
    }


    public String getDocNo() {
        return docNo;
    }

    public void addEntitysToDoc(HashSet<String> entitysToAdd)
    {
        for(String term: entitysToAdd)
        {
            if(allEntitysInDoc.containsKey(term))
            {
                int entityTF = allEntitysInDoc.get(term);
                allEntitysInDoc.replace(term,entityTF,entityTF+1);
            }
            else
            {
                allEntitysInDoc.put(term,1);
            }
        }
    }


    public HashMap<String, Integer> getAllEntitysInDoc() {
        return allEntitysInDoc;
    }

    public String[] getHeadLine() {
        return this.docTitle;
    }
}
