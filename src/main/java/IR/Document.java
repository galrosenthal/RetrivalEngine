package IR;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Objects;

/**
 * IR.Document Class is representing a document in the corpus
 * it has 4 parameters:
 * termsDictonary - HashMap that hold all the Terms in the Doc and how many times in it.
 * allDocElements - Each doc has many elements in it (DocNumber,Headers,text...etc') so this param is holding all of them
 * docText - The text of the IR.Document
 * docNo - The DocNumber of the IR.Document
 * @see Element
 * @see HashMap
 * @see Term
 */
public class Document {


    //    private ConcurrentHashMap
    private HashMap<Term,Integer> termsDictonary;// Each IR.Term has an Integer that presets how many times this IR.Term is present in the IR.Document

    private Elements allDocElements;
    private Element docText;
    private Element docNo;

    public Document(Element fileDocInCorpus) {
        this.termsDictonary = new HashMap<>();
        this.allDocElements = fileDocInCorpus.getAllElements();

        /**
         * searches for the element \"Text\" and \"docNo\"
         */
        for (Element elem :
                allDocElements) {
            if (elem.tag().equals("text"))
            {
                docText = elem;
            }
            else if(elem.tag().equals("docno"))
            {
                docNo = elem;
            }
        }
    }

    public Element getDocText() {
        return docText;
    }

    public Element getDocNo() {
        return docNo;
    }

    /**
     * if newTerm is not in the Doc termsDictionary it adds to the Dictionary with value 1
     * @param newTerm - a IR.Term that suppose to be in this Doc text docText
     */
    public void insertFoundTermInDoc(Term newTerm)
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
    }

    /**
     * if the existingTerm is already in the termsDictionary its value is increased by 1
     * @param existingTerm - a term that is already inside the dictionary
     */
    public void increaseTermCountInDoc(Term existingTerm)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;
        Document document = (Document) o;
        return Objects.equals(termsDictonary, document.termsDictonary) &&
                Objects.equals(allDocElements, document.allDocElements);
    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(termsDictonary, allDocElements);
//    }
}
