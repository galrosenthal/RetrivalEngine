package Ranker;

import Indexer.DocumentIndexer;

import java.util.ArrayList;
import java.util.HashMap;

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


    public ArrayList<String> rankQueryDocs(HashMap<String,String> termsAndLinesFromPost,HashMap<String,String> query)
    {
        //TODO: Maybe rank using priorityQueue reversed order(Max) or regular(Min)
        // for Max -> PriorityQueue<Integer> pQueue =  new PriorityQueue<Integer>(Collections.reverseOrder());
        // for Min -> PriorityQueue<Integer> pQueue =  new PriorityQueue<Integer>();

        ArrayList<String> docListRanked = new ArrayList<>();
        ArrayList<String> queryDocList = new ArrayList<>();
        double k = 1.5;
        double b = 0.75;
        double sumOfBM25 = 0;
        DocumentIndexer docIndexer = DocumentIndexer.getInstance();
        docIndexer.loadDictionaryFromDisk();

        for(String term: termsAndLinesFromPost.keySet())
        {
            //queryDocList.addAll(Arrays.asList(termsAndLinesFromPost.get(term).split(";")));


        }

//        docListRanked = bm25(queryDocList);


        //TODO: return only first 50 of them
        return docListRanked;
    }

}
