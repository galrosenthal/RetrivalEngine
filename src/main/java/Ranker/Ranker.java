package Ranker;

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


    public ArrayList<String> rankQueryDocs(HashMap<String,String> termsAndLinesFromPost)
    {
        ArrayList<String> docListRanked = new ArrayList<>();
        ArrayList<String> queryDocList = new ArrayList<>();

        for(String term: termsAndLinesFromPost.keySet())
        {
            for(String docParams: termsAndLinesFromPost.get(term).split(";"))
            {
                queryDocList.add(docParams.split("#")[0]);
            }
        }

        docListRanked = bm25(queryDocList);


        //TODO: return only first 50 of them
        return docListRanked;
    }

    private ArrayList<String> bm25(ArrayList<String> queryDocList) {
        //TODO: Maybe rank using priorityQueue reversed order
        // PriorityQueue<Integer> pQueue =  new PriorityQueue<Integer>(Collections.reverseOrder());
        ArrayList<String> rankedDocs = new ArrayList<>();


        return null;
    }
}
