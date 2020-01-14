package Ranker;

/**
 * Class that represents Ranked Entity in Document
 * implements {@link Comparable} interface in order to use this class in {@link java.util.PriorityQueue}
 */
public class RankedEntity implements Comparable<RankedEntity>{

    private String entityTerm;
    private double entityRank;

    /**
     * Contructs a Ranked Entity with its Term and specific rank
     * @param entityTerm - The Entity Term
     * @param entityRank - The Entity Rank
     */
    public RankedEntity(String entityTerm, double entityRank) {
        this.entityTerm = entityTerm;
        this.entityRank = entityRank;
    }

    public String getEntityTerm() {
        return entityTerm;
    }


    public double getEntityRank() {
        return entityRank;
    }


    /**
     * Used to compare 2 RankedEntity object by their rank
     * @param rankedEntity The other RankedEntity to compare to
     * @return value by {@link Double}.compare function
     */
    @Override
    public int compareTo(RankedEntity rankedEntity) {
        return Double.compare(entityRank,rankedEntity.entityRank);
    }
}
