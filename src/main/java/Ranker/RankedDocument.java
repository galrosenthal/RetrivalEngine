package Ranker;

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
