package Ranker;

/**
 * This Class is representing a ranked document for the Ranker class
 * it is implementing Comparable so it could be inserted into
 * PriorityQueue and be compared by the BM25 value
 */
public class RankedDocument implements Comparable<RankedDocument>
{
    private String docId;
    private double bm25Value;

    public RankedDocument(String docId, double bm25Value) {
        this.docId = docId;
        this.bm25Value = bm25Value;
    }

    public String getDocId() {
        return docId;
    }

    @Override
    public int compareTo(RankedDocument rankedDocument)
    {
        if(this.bm25Value > rankedDocument.bm25Value)
        {
            return 1;
        }
        if(bm25Value < rankedDocument.bm25Value)
        {
            return -1;
        }
        return 0;
    }
}
