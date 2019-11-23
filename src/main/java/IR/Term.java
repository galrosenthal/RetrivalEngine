package IR;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * IR.Term represents a word in the corpus that is significant to the data
 * it has 2 parameters
 * wordValue - String that represents the word
 * docsCount - Integer that represents the number of Documents this IR.Term is in.
 * @see Document
 */
public class Term {

    private String wordValue;
    private int docsCount;
    private List<Document> allPresentDocs;


    public Term(String wordValue) {
        this.wordValue = wordValue;
        this.allPresentDocs = new ArrayList<>();
        this.docsCount = allPresentDocs.size();

    }

    public String getWordValue() {
        return wordValue;
    }

//    public void setWordValue(String wordValue) {
//        this.wordValue = wordValue;
//    }

    public int getDocsCount() {
        return docsCount;
    }

    public void increaseTermDocCount(Document docTermIn)
    {
        if(!allPresentDocs.contains(docTermIn))
        {
            allPresentDocs.add(docTermIn);
        }
    }


    @Override
    public String toString() {
        return "IR.Term{" +
                "word='" + wordValue + '\'' +
                ", docsCount=" + docsCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Term)) return false;
        Term term = (Term) o;
        return getDocsCount() == term.getDocsCount() &&
                getWordValue().equals(term.getWordValue()) &&
                allPresentDocs.equals(term.allPresentDocs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWordValue(), getDocsCount());
    }
}
