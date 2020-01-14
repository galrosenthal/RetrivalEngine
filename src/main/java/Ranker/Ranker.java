package Ranker;

import IR.DocumentInfo;
import Indexer.DocumentIndexer;
import Indexer.Indexer;

import java.util.*;
import java.util.regex.Pattern;

/**
 * This Class is a Singleton class that is capable of ranking Corpus Documents against a given Query
 */
public class Ranker {


    private static volatile Ranker mInstance;
    private HashMap<String,String> query;

    private final double weightForQuery = 0.8;
    private final double weightForDescription = 0.3;
    private final double weightForSemantic = 0.6;
    private final double weightQueryAndDescription = 1;

    private static Pattern semiCloneSplitter = Pattern.compile(";");
    private static Pattern hashtagSplitter = Pattern.compile("#");


    private Ranker() {
        query = new HashMap<>();
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
     * @param searchedQuery HashMap that contains all the term and the line with the tf in the query
     * @return ArrayList<docId> which contains the 50 Most Ranked docs by BM25 formula
     */
    public ArrayList<String> rankQueryDocs(HashMap<String,String> termsAndLinesFromPost,HashMap<String,String> searchedQuery) {
        try {

            System.out.println("Ranker: ------------------Started Ranking " + searchedQuery.keySet().size() + " term in query-------------------");
            long startRanking = System.nanoTime();
            ArrayList<String> docListRanked;
            query.putAll(searchedQuery);
//            System.out.println("Ranker: getting HashMap<DocID,List<Pair<Term,TermTF in Doc>>>");
            long startDocReverse = System.nanoTime();
            HashMap<String, HashMap<String,Integer>> docToTermsInQry = getDocToTerm(termsAndLinesFromPost);
            long endDocReverse = System.nanoTime();
//            System.out.println("Ranker: reversing Doc took: " + (endDocReverse - startDocReverse)/1000000000 + "s");
            DocumentIndexer docIndexer = DocumentIndexer.getInstance();
            docIndexer.loadDictionaryFromDisk();
            int M = docIndexer.getSizeOfDictionary();
            double docAvgLength = docIndexer.getAvgLengthOfDoc();
            PriorityQueue<RankedDocument> rankingQueue = new PriorityQueue<>(Comparator.reverseOrder());


            //Ranking All of the Docs using BM25
//            System.out.println("Ranker: Start Ranknig specific " + docToTermsInQry.keySet().size() + " Docs");
            long spcfcRank = System.nanoTime();

            double alpha = 0.8;
            for(String docId: docToTermsInQry.keySet())
            {
                String[] headLineOfDoc = docIndexer.getDocumentInfoOfDoc(docId).getHeadLine();
                int maxTfInDoc = docIndexer.getDocumentInfoOfDoc(docId).getMaxTfOfTerm();
                int docLength = docIndexer.getLengthOfDoc(docId);
                double sumOfBM25 = 0;
                HashMap<String,Integer> allTermsInDocQuery = docToTermsInQry.get(docId);
                for (String termAndTf: allTermsInDocQuery.keySet())
                {
                    int tfInDoc = allTermsInDocQuery.get(termAndTf);
                    int termDf = termsAndLinesFromPost.get(termAndTf).split(";").length;
                    double cwq = Double.parseDouble(query.get(termAndTf).split("#")[1]);

                    long calcBM25 = System.nanoTime();
                    sumOfBM25 += alpha*calcBM25(M,docAvgLength,docLength,tfInDoc,termDf,cwq) + (1-alpha)*calcRankByHeadline(headLineOfDoc,maxTfInDoc,tfInDoc,termAndTf);
//                    System.out.println("Calculation BM25 for "+ docId + ", took: " + (System.nanoTime() - calcBM25)/1000000000 + "s");
                }
                rankingQueue.add(new RankedDocument(docId,sumOfBM25));
            }
            long spcfcRankEnd = System.nanoTime();
//            System.out.println("Ranker: Ranking whole docs took: " + (spcfcRankEnd - spcfcRank)/1000000000 + "s");

            //Get only the 50 highest ranked docs
//            System.out.println("Ranker: Getting 50Ranked docs");
            long get50rank = System.nanoTime();
            docListRanked = get50RankedDocs(rankingQueue);
            long get50rankEnd = System.nanoTime();
//            System.out.println("Ranker: Getting 50Ranked took: " + (get50rankEnd - get50rank)/1000000000 + "s");
            System.out.println("Ranker-----------Ranking Ended took: " + (get50rankEnd - startRanking)/1000000000 + "s--------------");
            return docListRanked;

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }











//
//
//    /**
//     * This Function is ranking the docs that are related to the term found in the query
//     * @param termsAndLinesFromPost HashMap that contains all the terms and the posting file lines related to them
//     * @param searchedQuery HashMap that contains all the term and the line with the tf in the query
//     * @param queryDescription
//     * @param descriptionTermsAndLines
//     * @param querySemantic
//     * @return ArrayList<docId> which contains the 50 Most Ranked docs by BM25 formula
//     */
//    public ArrayList<String> rankQueryDocs(HashMap<String,String> termsAndLinesFromPost,HashMap<String,String> searchedQuery,
//                                           HashMap<String,String> queryDescription, HashMap<String,String> descriptionTermsAndLines,
//                                           HashMap<String,String> querySemantic, HashMap<String,String> semanticTermsAndLines) {
//        try {
//
//            System.out.println("Ranker: ------------------Started Ranking " + searchedQuery.keySet().size() + " term in query-------------------");
//            long startRanking = System.nanoTime();
//            ArrayList<String> docListRanked;
////            query.putAll(searchedQuery);
////            System.out.println("Ranker: getting HashMap<DocID,List<Pair<Term,TermTF in Doc>>>");
//            long startDocReverse = System.nanoTime();
//            HashMap<String, HashMap<String,Integer>> docToTermsInQry = getDocToTerm(termsAndLinesFromPost);
//            HashMap<String, HashMap<String,Integer>> docToTermsInDescription = getDocToTerm(descriptionTermsAndLines);
//            //HashMap<String, ArrayList<Pair<String, Integer>>> docToTermsInSemantic = getDocToTerm(semanticTermsAndLines);
//            long endDocReverse = System.nanoTime();
////            System.out.println("Ranker: reversing Doc took: " + (endDocReverse - startDocReverse)/1000000000 + "s");
//            DocumentIndexer docIndexer = DocumentIndexer.getInstance();
//            docIndexer.loadDictionaryFromDisk();
//            int M = docIndexer.getSizeOfDictionary();
//            double docAvgLength = docIndexer.getAvgLengthOfDoc();
//            PriorityQueue<RankedDocument> rankingQueue = new PriorityQueue<>(Comparator.reverseOrder());
//
//            //Union the docs received from query and from description
//            Set<String> docFromQuerySet = new HashSet<>(docToTermsInQry.keySet());
//            Set<String> docFromDescriptionSet = new HashSet<>(docToTermsInDescription.keySet());
//            docFromQuerySet.addAll(docFromDescriptionSet);
//
//            //Union the HashMap of array lists
//            HashMap<String, HashMap<String,Integer>> unionQueryAndDescription = docToTermsInQry;
//            unionQueryAndDescription.putAll(docToTermsInDescription);
//
//            //Ranking All of the Docs using BM25
////            System.out.println("Ranker: Start Ranknig specific " + docToTermsInQry.keySet().size() + " Docs");
//            long spcfcRank = System.nanoTime();
//
//            rankingQAdding(termsAndLinesFromPost, searchedQuery, queryDescription, descriptionTermsAndLines, docToTermsInQry, docToTermsInDescription, docIndexer, M, docAvgLength, rankingQueue, docFromQuerySet, unionQueryAndDescription);
//            long spcfcRankEnd = System.nanoTime();
////            System.out.println("Ranker: Ranking whole docs took: " + (spcfcRankEnd - spcfcRank)/1000000000 + "s");
//
//            //Get only the 50 highest ranked docs
////            System.out.println("Ranker: Getting 50Ranked docs");
//            long get50rank = System.nanoTime();
//            docListRanked = get50RankedDocs(rankingQueue);
//            long get50rankEnd = System.nanoTime();
////            System.out.println("Ranker: Getting 50Ranked took: " + (get50rankEnd - get50rank)/1000000000 + "s");
//            System.out.println("Ranker-----------Ranking Ended took: " + (get50rankEnd - startRanking)/1000000000 + "s--------------");
//            return docListRanked;
//
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private void rankingQAdding(HashMap<String, String> termsAndLinesFromPost, HashMap<String, String> searchedQuery, HashMap<String, String> queryDescription, HashMap<String, String> descriptionTermsAndLines, HashMap<String, HashMap<String, Integer>> docToTermsInQry, HashMap<String, HashMap<String, Integer>> docToTermsInDescription, DocumentIndexer docIndexer, int m, double docAvgLength, PriorityQueue<RankedDocument> rankingQueue, Set<String> docFromQuerySet, HashMap<String, HashMap<String, Integer>> unionQueryAndDescription) {
//        for(String docId: docFromQuerySet)
//        {
//
//            String[] headLineOfDoc = docIndexer.getDocumentInfoOfDoc(docId).getHeadLine();
//            int maxTfInDoc = docIndexer.getDocumentInfoOfDoc(docId).getMaxTfOfTerm();
//            int docLength = docIndexer.getLengthOfDoc(docId);
//            double sumOfBM25 = 0;
//            HashMap<String,Integer> allTermsInDocQuery = unionQueryAndDescription.get(docId);
//            sumOfBM25 = getSumOfBM25(termsAndLinesFromPost, searchedQuery, queryDescription, descriptionTermsAndLines, docToTermsInQry, docToTermsInDescription, m, docAvgLength, unionQueryAndDescription, docId, headLineOfDoc, maxTfInDoc, docLength, sumOfBM25, allTermsInDocQuery);
//            rankingQueue.add(new RankedDocument(docId,sumOfBM25));
//        }
//    }
//
//    private double getSumOfBM25(HashMap<String, String> termsAndLinesFromPost, HashMap<String, String> searchedQuery, HashMap<String, String> queryDescription, HashMap<String, String> descriptionTermsAndLines, HashMap<String, HashMap<String, Integer>> docToTermsInQry, HashMap<String, HashMap<String, Integer>> docToTermsInDescription, int m, double docAvgLength, HashMap<String, HashMap<String, Integer>> unionQueryAndDescription, String docId, String[] headLineOfDoc, int maxTfInDoc, int docLength, double sumOfBM25, HashMap<String, Integer> allTermsInDocQuery) {
//        double alpha;
//        for (String termAndTf: allTermsInDocQuery.keySet())
//        {
//            double cwq = 0;
//            double termDf = 0;
//            String[] descriptionTermsLinesSplitted ;
//            String[] queryDescriptionSplitted ;
//            String[] querySearchSplitted;
//            String[] termsSplitted;
//            if(docToTermsInQry.containsKey(docId))
//            {
//
//                if(docToTermsInDescription.containsKey(docId))
//                {
//                    descriptionTermsLinesSplitted = descriptionTermsAndLines.get(termAndTf).split(";");
//                    queryDescriptionSplitted = queryDescription.get(termAndTf).split("#");
//                    alpha = weightQueryAndDescription;
//                            termDf += alpha*descriptionTermsLinesSplitted.length;
////                            termDf += (alpha * StringUtils.split(descriptionTermsAndLines.get(termAndTf),";").length);
////                    termDf += alpha*semiCloneSplitter.split(descriptionTermsAndLines.get(termAndTf)).length;
//                            cwq += Double.parseDouble(queryDescriptionSplitted[1]);
////                    cwq += Double.parseDouble(StringUtils.split(queryDescription.get(termAndTf),"#")[1]);
//                }
//                else{
//                    querySearchSplitted = searchedQuery.get(termAndTf).split("#");
//                    termsSplitted = termsAndLinesFromPost.get(termAndTf).split(";");
//                    alpha = weightForQuery;
//                        cwq += Double.parseDouble(querySearchSplitted[1]);
////                        cwq += Double.parseDouble(StringUtils.split(searchedQuery.get(termAndTf),"#")[1]);
////                    cwq += Double.parseDouble(hashtagSplitter.split(searchedQuery.get(termAndTf))[1]);
//                        termDf += termsSplitted.length;
////                    termDf += semiCloneSplitter.split(termsAndLinesFromPost.get(termAndTf)).length;
////                    termDf += StringUtils.split(termsAndLinesFromPost.get(termAndTf)).length;
//                }
//            }
//            else if(docToTermsInDescription.containsKey(docId))
//            {
//                descriptionTermsLinesSplitted = descriptionTermsAndLines.get(termAndTf).split(";");
//                queryDescriptionSplitted = queryDescription.get(termAndTf).split("#");
//                //Query is description
//                        cwq = Double.parseDouble(queryDescriptionSplitted[1]);
////                cwq = Double.parseDouble(hashtagSplitter.split(queryDescription.get(termAndTf))[1]);
////                cwq = Double.parseDouble(StringUtils.split(queryDescription.get(termAndTf))[1]);
//                alpha = weightForDescription;
//                        termDf = alpha*descriptionTermsLinesSplitted.length;
////                termDf = alpha*semiCloneSplitter.split(descriptionTermsAndLines.get(termAndTf)).length;
////                termDf = alpha*StringUtils.split(descriptionTermsAndLines.get(termAndTf)).length;
//            }
//            else
//            {
//                alpha = weightForSemantic;
//            }
//            int tfInDoc = unionQueryAndDescription.get(docId).get(termAndTf);
//
//
//
//            long calcBM25 = System.nanoTime();
//            sumOfBM25 += alpha*calcBM25(m,docAvgLength,docLength,tfInDoc,termDf,cwq) + (1-alpha)*calcRankByHeadline(docLength,headLineOfDoc,maxTfInDoc,tfInDoc,termAndTf);
////                    System.out.println("Calculation BM25 for "+ docId + ", took: " + (System.nanoTime() - calcBM25)/1000000000 + "s");
//        }
//        return sumOfBM25;
//    }





    /**
     * Calculating Rank for the Doc according to it HeadLine, if it does not have Head Line the rank is 0.
     * @return the rank of the doc
     */
    private double calcRankByHeadline(String[] headLineOfDoc, int maxTfInDoc, int tfInDoc,String term) {
        if (headLineOfDoc == null ){
            return 0;
        }
        double rankByHeadLine = 0;
        int countTermLocationInHeadLine = 0;
        for(String headTerm : headLineOfDoc)
        {
            countTermLocationInHeadLine++;
            if(headTerm.equalsIgnoreCase(term))
            {
                rankByHeadLine += (double)countTermLocationInHeadLine/headLineOfDoc.length + (double)tfInDoc/maxTfInDoc;
            }
        }

        return rankByHeadLine;
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
            if(count50Docs == 50)
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
    private double calcBM25(int corpusSize, double docAvgLength, int docLength, int tfInDoc, double termDf,double cwq) {
        double k = 1.8;
        double b = 0.75;
        double sum = 0;
        sum = cwq*(((k+1)*tfInDoc)/(tfInDoc+k*(1-b+b*docLength/docAvgLength)))*(Math.log((double) (corpusSize+1)/(termDf)));

        return sum;
    }


    /**
     * Creates a HashMap of the docIds to an HashMap<Term,TermTF in Doc>
     * @param termsAndLinesFromPost a HashMap<Term,Line From Posting File*>
     * @return the HashMap that was created
     * line in posting file looks like this, ex':
     * FBIS3-67#1;FBIS4-55333#1;FBIS4-8952#6
     */
    private HashMap<String, HashMap<String, Integer>> getDocToTerm(HashMap<String, String> termsAndLinesFromPost)
    {
        HashMap<String, HashMap<String, Integer>> docToTerm = new HashMap<>();
        for(String term: termsAndLinesFromPost.keySet())
        {
            for(String docParams: semiCloneSplitter.split(termsAndLinesFromPost.get(term)))
            {
                String docId = hashtagSplitter.split(docParams)[0];
                int termTfInDoc = Integer.parseInt(hashtagSplitter.split(docParams)[1]);
                if(docToTerm.containsKey(docId))
                {
                    docToTerm.get(docId).put(term,termTfInDoc);
                }
                else
                {
                    HashMap<String, Integer> newList = new HashMap<>();
                    newList.put(term,termTfInDoc);
                    docToTerm.put(docId,newList);
                }
            }
        }
        return docToTerm;
    }

    public void resetQuery()
    {
        query = new HashMap<>();
    }


    /**
     * foreach entity in the doc, if it is in the query retrieve it
     * if found 5 entitys done
     * if there are no entity in the query, retrieve 5 entitys by the TF in the doc
     * if there a less than 5 entitys in the Query retrive all of them and add the remaining entity by the TF in the doc
     *
     * TF/NUM_OF_ENTITY
     * TF/DOC_LENGTH
     * TF/TOTAL_TF
     *
     * @param docID docID of the document to search for
     * @return
     */
    public HashMap<String,Double> rankEntitysOfDoc(String docID)
    {
        DocumentIndexer docIndexer = DocumentIndexer.getInstance();
        docIndexer.loadDictionaryFromDisk();
        Indexer myIndexer = Indexer.getInstance();

        HashMap<String,Double> rankedEntitys = new HashMap<>();

        PriorityQueue<RankedEntity> entitiesPriorirtyQ = new PriorityQueue<RankedEntity>(Comparator.reverseOrder());

        DocumentInfo docInfo = docIndexer.getDocumentInfoOfDoc(docID);
        if(docInfo == null)
        {
            return null;
        }

        //All Entitys in the Doc
        Set<String> docEntitys = docInfo.getAllEntitysInDoc().keySet();
        Set<String> allEntitysInCorpus = myIndexer.getEntitiesInCorpus();

        //Get only the intersection of entities in the corpus
        docEntitys.retainAll(allEntitysInCorpus);

        double docLength = docInfo.getDocLength();

        for (String entity :
                docEntitys) {
            int tf = docInfo.getAllEntitysInDoc().get(entity);
            int totalTf =  myIndexer.getTotalTF(entity);

            double rankOfEntity = (double)(tf)/(totalTf) + (tf)/(docLength) + (double) (tf)/(docEntitys.size());
            RankedEntity entty = new RankedEntity(entity,rankOfEntity);

            entitiesPriorirtyQ.add(entty);
        }


        int countEntities = 0;
        while(countEntities < 5 && !entitiesPriorirtyQ.isEmpty())
        {
            RankedEntity re = entitiesPriorirtyQ.poll();
            rankedEntitys.put(re.getEntityTerm(),re.getEntityRank());
            countEntities++;
        }

        return rankedEntitys;
    }



}
