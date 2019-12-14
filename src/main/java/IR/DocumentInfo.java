package IR;

import java.io.Serializable;

public class DocumentInfo implements Serializable
{


    private String docDate;
    private int numUniqeTerms;
    private String maxTfTerm;
    private String[] docTextArry;
    private String docNo;

    public DocumentInfo(Document doc) {
        this.docNo = doc.getDocNo();
        this.docTextArry = doc.getTextArray();
        this.maxTfTerm = doc.getMaxTfTerm();
        this.numUniqeTerms = doc.getNumUniqeTerm();
        this.docDate = doc.getDocDate();
    }

    public String getDocDate() {
        return docDate;
    }

    public int getNumUniqeTerms() {
        return numUniqeTerms;
    }

    public String getMaxTfTerm() {
        return maxTfTerm;
    }

    public String[] getDocTextArry() {
        return docTextArry;
    }

    public String getDocNo() {
        return docNo;
    }
}
