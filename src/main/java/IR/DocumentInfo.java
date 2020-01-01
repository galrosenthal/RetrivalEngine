package IR;

import java.io.Serializable;

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
    private int namUniqueTerms;
    private String maxTfTerm;
    private int maxTfOfTerm;
    private String docNo;

    public DocumentInfo(Document doc) {
        this.docNo = doc.getDocNo();
        this.maxTfTerm = doc.getMaxTfTerm();
        this.namUniqueTerms = doc.getNumUniqeTerm();
        this.docDate = doc.getDocDate();
        this.maxTfOfTerm = doc.getMaxTF();
    }

    public int getMaxTfOfTerm() {
        return maxTfOfTerm;
    }

    public String getDocDate() {
        return docDate;
    }

    public int getNamUniqueTerms() {
        return namUniqueTerms;
    }

    public String getMaxTfTerm() {
        return maxTfTerm;
    }


    public String getDocNo() {
        return docNo;
    }
}
