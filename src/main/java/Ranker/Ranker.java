package Ranker;

import Indexer.DocumentIndexer;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * This Class is a Singleton class that is capable of ranking Corpus Documents against a given Query
 */
public class Ranker {


    private static volatile Ranker mInstance;

    private Ranker() {
    }

    public static Ranker getInstance() {
        if (mInstance == null) {
            synchronized (Ranker.class) {
                if (mInstance == null) {
                    mInstance = new Ranker();
                }
            }
        }
        return mInstance;
    }


    /**
     * This Function is ranking the docs that are related to the term found in the query
     * @param termsAndLinesFromPost HashMap that contains all the terms and the posting file lines related to them
     * @param query HashMap that contains all the term and the line with the tf in the query
     * @return ArrayList<docId> which contains the 50 Most Ranked docs by BM25 formula
     */
    public ArrayList<String> rankQueryDocs(HashMap<String,String> termsAndLinesFromPost,HashMap<String,String> query) {
        try {
            ArrayList<String> docListRanked = new ArrayList<>();
            HashMap<String, ArrayList<Pair<String, Integer>>> docToTermsInQry = getDocToTerm(termsAndLinesFromPost);
            DocumentIndexer docIndexer = DocumentIndexer.getInstance();
            docIndexer.loadDictionaryFromDisk();
            int M = docIndexer.getSizeOfDictionary();
            int docAvgLength = docIndexer.getAvgLengthOfDoc();
            PriorityQueue<RankedDocument> rankingQueue = new PriorityQueue<>(Comparator.naturalOrder());


            //Ranking All of the Docs using BM25
            for(String docId: docToTermsInQry.keySet())
            {
                int docLength = docIndexer.getLengthOfDoc(docId);
                double sumOfBM25 = 0;
                ArrayList<Pair<String,Integer>> allTermsInDocQuery = docToTermsInQry.get(docId);
                for (Pair<String,Integer> termAndTf: allTermsInDocQuery)
                {
                    int tfInDoc = termAndTf.getValue();
                    int termDf = termsAndLinesFromPost.get(termAndTf.getKey()).split(";").length;
                    double cwq = Double.parseDouble(query.get(termAndTf.getKey()).split("#")[1]);

                    sumOfBM25 += calcBM25(M,docAvgLength,docLength,tfInDoc,termDf,cwq);
                }
                rankingQueue.add(new RankedDocument(docId,sumOfBM25));
            }

            //Get only the 50 highest ranked docs
            docListRanked = get50RankedDocs(rankingQueue);
            return docListRanked;

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Iterates over the PriorityQueue and returns only the 50 Highest Ranked Docs
     * or all of them if there are less than 50 in the Q
     * @param rankingQueue Priority Queue which contains all the Ranked Documents Prioritized
     * @return List of the 50 most Ranked Docs
     */
    private ArrayList<String> get50RankedDocs(PriorityQueue<RankedDocument> rankingQueue)
    {
        int count50Docs = 0;
        ArrayList<String> listOfRankedDocs = new ArrayList<>();
        while(!rankingQueue.isEmpty())
        {
            RankedDocument rd = rankingQueue.poll();

            listOfRankedDocs.add(rd.getDocId());
            count50Docs++;

            if(count50Docs > 50)
            {
                break;
            }
        }

        return listOfRankedDocs;

    }

    /**
     *
     * @param corpusSize - The size of the corpus
     * @param docAvgLength - The Average Length of a document in the corpus
     * @param docLength - The length of the Specific Document currently calculating BM25 for
     * @param tfInDoc - The Term Frequency of the specific term in the specific doc
     * @param termDf - The Doc Frequency of the specific term currently calculating
     * @param cwq - The Specific Term Frequency in the Query
     * @return value of the BM25 summation
     */
    private double calcBM25(int corpusSize, int docAvgLength, int docLength, int tfInDoc, int termDf,double cwq) {
        double k = 1.5;
        double b = 0.75;
        double sum = 0;
        sum = cwq*(((k+1)*tfInDoc)/(tfInDoc+k*(1-b+b*docLength/docAvgLength)))*(Math.log((double) (corpusSize+1)/(termDf)));

        return sum;
    }


    /**
     * Creates a HashMap of the docIds to an ArrayList<Pair<Term,TermTF in Doc>>
     * @param termsAndLinesFromPost a HashMap<Term,Line From Posting File*>
     * @return the HashMap that was created
     * line in posting file looks like this, ex':
     * FBIS3-67#1;FBIS4-55333#1;FBIS4-8952#6
     */
    private HashMap<String, ArrayList<Pair<String, Integer>>> getDocToTerm(HashMap<String, String> termsAndLinesFromPost)
    {
        HashMap<String, ArrayList<Pair<String, Integer>>> docToTerm = new HashMap<>();
        for(String term: termsAndLinesFromPost.keySet())
        {
            for(String docParams: termsAndLinesFromPost.get(term).split(";"))
            {
                String docId = docParams.split("#")[0];
                int termTfInDoc = Integer.parseInt(docParams.split("#")[1]);
                if(docToTerm.containsKey(docId))
                {
                    docToTerm.get(docId).add(new Pair<>(term,termTfInDoc));
                }
                else
                {
                    ArrayList<Pair<String,Integer>> newList = new ArrayList<>();
                    Pair<String,Integer> termToTf = new Pair<>(term,termTfInDoc);
                    newList.add(termToTf);
                    docToTerm.put(docId,newList);
                }
            }
        }
        return docToTerm;
    }

}
