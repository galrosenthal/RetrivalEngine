package Ranker;

import Indexer.DocumentIndexer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

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

        try {
            ArrayList<String> docListRanked = new ArrayList<>();
            double k = 1.5;
            double b = 0.75;
            double sumOfBM25 = 0;
            DocumentIndexer docIndexer = DocumentIndexer.getInstance();
            docIndexer.loadDictionaryFromDisk();
            int M = docIndexer.getSizeOfDictionary();
            int docAvgLength = docIndexer.getAvgLengthOfDoc();
            PriorityQueue<RankedDocument> rankingQueue = new PriorityQueue<>(Comparator.naturalOrder());

            //Posting Line looks like:
            // FBIS3-67#1;FBIS4-55333#1;FBIS4-8952#6
            for (String term : termsAndLinesFromPost.keySet()) {
                //queryDocList.addAll(Arrays.asList(termsAndLinesFromPost.get(term).split(";")));
                //cwq = c(w,q) frequency of the word in the query
                double cwq = Double.parseDouble(query.get(term).split("#")[1]);
                //termDf = document frequency of word in corpus
                int termDf = termsAndLinesFromPost.get(term).split(";").length;
                //Dictionary error
                if(termDf == 0)
                {
                    throw new Exception("Something went wrong with the Dictionary");
                }

                for (String docParams : termsAndLinesFromPost.get(term).split(";"))
                {

                    String docId = docParams.split("#")[0];
                    int docLength = docIndexer.getLengthOfDoc(docId);
                    //termTfInDoc = c(i,j) frequency of the word i in doc j
                    int termTfInDoc = Integer.parseInt(docParams.split("#")[1]);
                    //The formula in the png of BM25
                    sumOfBM25 = cwq*(((k+1)*termTfInDoc)/(termTfInDoc+k*(1-b+b*docLength/docAvgLength)))*(Math.log((M+1)/(termDf)));
                    rankingQueue.add(new RankedDocument(docId,sumOfBM25));
                }

            }

            int count50RankedDocs = 0;
            while(!rankingQueue.isEmpty())
            {
                RankedDocument rd = rankingQueue.poll();
                docListRanked.add(rd.getDocId());
                count50RankedDocs++;
                if(count50RankedDocs > 50)
                {
                    break;
                }
            }

            //TODO: return only first 50 of them
            return docListRanked;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
