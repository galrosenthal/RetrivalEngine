package Ranker;

public class RankedEntity implements Comparable<RankedEntity>{

    private String entityTerm;
    private double entityRank;
    private int totalTfInCorpus;

    public RankedEntity(String entityTerm, double entityRank, int totalTfInCorpus) {
        this.entityTerm = entityTerm;
        this.entityRank = entityRank;
        this.totalTfInCorpus = totalTfInCorpus;
    }

    public String getEntityTerm() {
        return entityTerm;
    }

    public void setEntityTerm(String entityTerm) {
        this.entityTerm = entityTerm;
    }

    public double getEntityRank() {
        return entityRank;
    }

    public void setEntityRank(double entityRank) {
        this.entityRank = entityRank;
    }

    public int getTotalTfInCorpus() {
        return totalTfInCorpus;
    }

    public void setTotalTfInCorpus(int totalTfInCorpus) {
        this.totalTfInCorpus = totalTfInCorpus;
    }

    @Override
    public int compareTo(RankedEntity re) {
        return Double.compare(entityRank,re.entityRank);
    }
}
