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

    /**
     * Contructs a Ranked Document with its Name and specific rank
     * @param docId - The Document Name
     * @param bm25Value - The Document Rank
     */
    public RankedDocument(String docId, double bm25Value) {
        this.docId = docId;
        this.bm25Value = bm25Value;
    }

    public String getDocId() {
        return docId;
    }

    /**
     * Used to compare 2 RankedDocument object by their rank
     * @param rankedDocument The other RankedDocument to compare to
     * @return value by {@link Double}.compare function
     */
    @Override
    public int compareTo(RankedDocument rankedDocument)
    {
        return Double.compare(this.bm25Value, rankedDocument.bm25Value);
    }
}
