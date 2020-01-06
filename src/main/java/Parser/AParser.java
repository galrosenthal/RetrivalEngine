package Parser;

import IR.Document;
import IR.DocumentInfo;
import Indexer.*;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Parser.AParser is Abstract class represents a parser and what should one have
 */
public abstract class AParser implements Runnable {

    protected final double BILLION = 1000000000;
    protected final double MILLION = 1000000;
    protected final double THOUSAND = 1000;
    protected boolean withStemm;
    protected char[] punctuations = {',','.',';',':','?','(',')','"','{','}','-',']','[','!','\t','\n','|','*','\'','+','/','_','`'};
    private String tfDelim = "#";
    protected String parseName;
    protected String[] docText;
    protected static HashSet<String> stopWords;
//    protected static HashSet<String> stopMWords;
//    protected ConcurrentHashMap<String,String> termsInText;
    protected HashMap<String,String> termsInText;
    protected static ConcurrentLinkedQueue<Document> docQueueWaitingForParse;
    protected static int numOfParsedDocInIterative;
    private Indexer myIndexer = Indexer.getInstance();
    private static final int numberOfDocsToPost = 100;
    private static final int numOfDocsToSave = 100000;
    protected volatile boolean stopThread = false;
    private boolean doneReadingDocs;
    public StringBuilder lastDocList;
    private static ReadWriteLock docEnqDeqLocker = new ReentrantReadWriteLock();
    protected static Semaphore termsInTextSemaphore = new Semaphore(1);
    protected static Semaphore allDocsSemaphore = new Semaphore(1);
    protected boolean isParsing = false;
    public static ConcurrentHashMap<String, DocumentInfo> allDocs;
    public static String pathToCorpus;


    //    public static ReadWriteLock termsInTextLock = new ReentrantReadWriteLock();


    protected AParser()
    {
//        termsInText = new ConcurrentHashMap<>();
        termsInText = new HashMap<>();
        docQueueWaitingForParse = new ConcurrentLinkedQueue<>();
        numOfParsedDocInIterative = 0;
//        createStopWords();
//        createMStopWords();
        doneReadingDocs = false;
        stopThread = false;
        allDocs = new ConcurrentHashMap<>();



    }


    /**
     * Stops the threads
     */
    public void stopThread()
    {
        doneReadingDocs = true;
        while(isParsing);
        releaseToIndexerFile();
        stopThread = true;

    }

    /**
     * Marks the Doc that was just parsed as parsed and gets all of its Info
     * @param doc a document to mark as parsed
     */
    protected void makeDocParsed(Document doc)
    {
        allDocsSemaphore.acquireUninterruptibly();
        allDocs.put(doc.getDocNo(),new DocumentInfo(doc));
        allDocsSemaphore.release();
    }


    /**
     * Checks if the queue is Empty
     * @return
     */
    public boolean isQEmpty()
    {
        docEnqDeqLocker.readLock().lock();
        boolean isItEmpty = docQueueWaitingForParse.isEmpty();
        docEnqDeqLocker.readLock().unlock();
        return isItEmpty;
    }

    /**
     * Enqueue a new Document to the tail of the queue
     * @param d
     * @return
     */
    public boolean enqueueDoc(Document d)
    {
        if(d != null && !docQueueWaitingForParse.contains(d))
        {
            docEnqDeqLocker.writeLock().lock();
            boolean inserted = docQueueWaitingForParse.add(d);
            docEnqDeqLocker.writeLock().unlock();
            return inserted;
        }
        return false;
    }

    /**
     * Dequeue first Document in the queue
     * @return
     */
    protected Document dequeueDoc()
    {
        docEnqDeqLocker.readLock().lock();
        Document dqd = docQueueWaitingForParse.poll();
        docEnqDeqLocker.readLock().unlock();
        return dqd;
    }


    /**
     * Enqueue the HashMap of term that was parsed in the current iterative to the indexer queue
     * if the numOfParsedDocInIterative is greater the numberOfDocsToPost(final)
     */
    protected void releaseToIndexerFile()
    {
        if(numOfParsedDocInIterative >= numberOfDocsToPost || doneReadingDocs)
        {
            termsInTextSemaphore.acquireUninterruptibly();
            if(!Indexer.getInstance().enqueue(termsInText))
            {
            }
            termsInText = new HashMap<>();
            numOfParsedDocInIterative = 0;
            termsInTextSemaphore.release();
//        }
//        if(numOfParsedDocInIterative >= numOfDocsToSave || doneReadingDocs) {
            allDocsSemaphore.acquireUninterruptibly();
//            System.out.println("releasing " + allDocs.size() + " doc map");
            if (!DocumentIndexer.enQnewDocs(allDocs)) {
            }
            allDocs = new ConcurrentHashMap<>();
            allDocsSemaphore.release();
        }
    }

    public void setPathToCorpus(String corpusPath) {
        pathToCorpus = corpusPath;
        createStopWords();
    }

    /**
     * Creates a HashSet that contains all the stopwords from the file <b>resources/stopWords.txt</b>
     */
    protected void createStopWords() {
        if (stopWords == null) {
            stopWords = new HashSet<>();


            try {
                File stopWordsFile = new File(pathToCorpus+"/../stop_words.txt");

                if (!stopWordsFile.exists()) {
                    System.out.println(stopWordsFile.getAbsolutePath());
                }
                BufferedReader stopWordsReader = new BufferedReader(new FileReader(stopWordsFile));

                String word = stopWordsReader.readLine();
                while (word != null) {
                    stopWords.add(word.toLowerCase());
                    stopWords.add(word);
                    stopWords.add(word.toUpperCase());
                    word = stopWordsReader.readLine();
                }

                stopWordsReader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * Creates a HashSet that contains more stopwords from the file <b>resources/moreStopWords.txt</b>
//     */
//    protected void createMStopWords() {
//        if (stopMWords == null) {
//            stopMWords = new HashSet<>();
//
//
//            try {
//                File stopWordsFile = new File(pathToCorpus+"/stop_words.txt");
//                if (!stopWordsFile.exists()) {
//                    System.out.println(stopWordsFile.getAbsolutePath());
//                }
//                BufferedReader stopWordsReader = new BufferedReader(new FileReader(stopWordsFile));
//
//                String word = stopWordsReader.readLine();
//                while (word != null) {
//                    stopMWords.add(word.toLowerCase());
//                    stopMWords.add(word);
//                    stopMWords.add(word.toUpperCase());
//                    word = stopWordsReader.readLine();
//                }
//
//                stopWordsReader.close();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * the abstract function that parses a document
     */
    public abstract void parse();

    /**
     * Gets a String and removes all of the Punctuations from the end of it
     * @param word a string to remove Punctuations from
     * @return the choped String
     */
    protected String chopDownLastCharPunc(String word) {

        if(word != null && word.length() >= 1)
        {
//            word = word.toLowerCase();
            while(isLastCharPunctuation(word))
            {
                word = word.substring(0,word.length()-1);
            }

        }
        return word;
    }


    /**
     * @param word a String to check if the last char of it is a Punctuatuion
     * @return true if the last char of word is Punctuation
     */
    protected boolean isLastCharPunctuation(String word) {
        if(word == null||word.length() == 0)
        {
            return false;
        }

        for (char punc :
                punctuations) {
            if(word.charAt(word.length()-1) == punc)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a String and removes all of the Punctuations from the start of it
     * @param word a string to remove Punctuations from
     * @return the choped String
     */
    protected String chopDownFisrtChar(String word) {
        if(word != null && word.length() >= 2)
        {
            while(isFirstCharPunctuation(word)){
                word = word.substring(1);
            }
        }
        return word;
    }

    /**
     * @use chopDownFirstChar(String word)
     */
    protected StringBuilder chopDownFisrtChar(StringBuilder word) {
        String choppedStringFromWord = chopDownFisrtChar(word.toString());
        StringBuilder choppedWord = new StringBuilder(choppedStringFromWord);
        return choppedWord;
    }


    /**
     * @param word a String to check if the first char of it is a Punctuatuion
     * @return true if the first char of word is Punctuation
     */
    protected  boolean isFirstCharPunctuation(String word) {
        if(word != null && word.length() >= 2)
        {
            word = word.toLowerCase();
            for (char punc :
                    punctuations) {
                if(word.charAt(0) == punc)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param word a String to check if it is a Fraction
     * @return true if the word is a Fraction (num1/num2)
     */
    protected boolean isFraction(String word){
        boolean isFraction = false;
        word = word.replaceAll(",","");
        String[] splittedFraction = word.split("/");
//        wordForSplitting.split(" ");

        if(splittedFraction.length == 2 && NumberUtils.isNumber(splittedFraction[0]) && NumberUtils.isNumber(splittedFraction[1]))
        {
            isFraction = true;
        }

        return isFraction;
    }


    /**
     * Gets a parsed term and inserting it to the Dictionary
     * @param term the Term to insert into the Dictionary
     * @param doc the doc the term was parsed from
     * @param parserName the parser name who parsed the Term
     */
    protected void parsedTermInsert(String term, Document doc, String parserName) {
        if(term.isEmpty())
            return;

        String currentDocNo = doc.getDocNo();
        if (termsInText.containsKey(term)) {

             String docList = termsInText.get(term);
            String[] docsSplitted =  docList.split(";");
            boolean docAlreadyParsed = false;
            int oldtf = 0;
            lastDocList = new StringBuilder("");

            for (String docParams:
                 docsSplitted) {
                String[] docAndtf = docParams.split(tfDelim);
                oldtf = Integer.parseInt(docAndtf[1]);
                if(docAndtf[0].equals(currentDocNo))
                {
                    oldtf += 1;
                    docAlreadyParsed = true;
                }
                lastDocList.append(docAndtf[0]).append(tfDelim).append(oldtf).append(tfDelim).append(parserName).append(";");
            }
            if(!docAlreadyParsed)
            {
                lastDocList.append(currentDocNo).append(tfDelim).append(1).append(tfDelim).append(parserName).append(";");
            }
            lastDocList = new StringBuilder(lastDocList.substring(0,lastDocList.length()-1));

            termsInText.replace(term,docList,lastDocList.toString());

        } else {
            lastDocList = new StringBuilder("");
            lastDocList.append(currentDocNo).append(tfDelim).append(1).append(tfDelim).append(parserName);
            termsInText.put(term, lastDocList.toString());
        }

        doc.insertFoundTermInDoc(term);
    }

    /**
     * Sets the stemming boolean
     * @param withStemm a boolean that sets the stemming option, whether to use stemming or not
     */
    public void setStemm(boolean withStemm) {
        this.withStemm = withStemm;
    }

    public HashMap<String, String> getTermsInText() {
        return termsInText;
    }
}
