package Parser;

import IR.Document;
import IR.Term;
import Indexer.Indexer;
import Indexer.ReadWriteTempDic;
import Tokenizer.Tokenizer;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AParser implements Runnable {

    protected final double BILLION = 1000000000;
    protected final double MILLION = 1000000;
    protected final double THOUSAND = 1000;
    protected char[] punctuations = {',','.',';',':','?','(',')','"','{','}','!','\t','\n'};
    private String tfDelim = "#";
    protected String parseName;
    protected String[] docText;
    protected Tokenizer toknizr = Tokenizer.getInstance();
    protected static HashSet<String> stopWords;
//    protected ConcurrentHashMap<String,String> termsInText;
    protected HashMap<String,String> termsInText;
//    private ConcurrentLinkedQueue<Document> docQueueWaitingForParse;
    private static Queue<Document> docQueueWaitingForParse;
    protected static int numOfParsedDocInIterative;
    private Indexer myIndexer = Indexer.getInstance();
    private static final int numberOfDocsToPost = 10000;
    protected boolean stopThread = false;
    protected ReadWriteTempDic myReadWriter = ReadWriteTempDic.getInstance();
    protected boolean doneReadingDocs;
    public StringBuilder lastDocList;
    private static ReadWriteLock termsInTextLocker;


    protected AParser()
    {
//        termsInText = new ConcurrentHashMap<>();
        termsInText = new HashMap<>();
//        docQueueWaitingForParse = new ConcurrentLinkedQueue<>();
        docQueueWaitingForParse = new ConcurrentLinkedQueue<>();
        numOfParsedDocInIterative = 0;
        createStopWords();
        doneReadingDocs = false;
        termsInTextLocker = new ReentrantReadWriteLock();


    }

    public void stopThread()
    {
        while(!docQueueWaitingForParse.isEmpty())
        {
            System.out.println("Waiting for parser");
        }
        doneReadingDocs = true;
        System.out.println("Releasing to index");
        releaseToIndexerFile();
        System.out.println("Released Map");
        stopThread = true;

    }

    public boolean isQEmpty()
    {
        return this.docQueueWaitingForParse.isEmpty();
    }
    /**
     * Checks if the queue is Empty
     * @return
     */
    protected boolean queueIsEmpty()
    {
        return this.docQueueWaitingForParse.isEmpty();
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
            return this.docQueueWaitingForParse.add(d);
        }
        return false;
    }

    /**
     * Dequeue first Document in the queue
     * @return
     */
    protected Document dequeueDoc()
    {
        return docQueueWaitingForParse.poll();
    }


    protected void releaseToIndexerFile()
    {
        if((numOfParsedDocInIterative >= numberOfDocsToPost || doneReadingDocs))
        {
//            if(this instanceof parseNumbers)
//            {
//
//            }
            System.out.println("Should enqueue");
            if(/*!myReadWriter.writeToDic(termsInText,getName()) ||*/ !Indexer.enqueue(termsInText))
            {
                System.out.println("Fuck it");
                //TODO: maybe throw exception?
            }
                System.out.println("Enqueued "+ numOfParsedDocInIterative +" to Indexer");
//            myIndexer.enqueue(termsInText);
//            termsInText = null;
//            termsInText = new ConcurrentHashMap<>();
            termsInTextLocker.writeLock().lock();
            termsInText = new HashMap<>();
            numOfParsedDocInIterative = 0;
            termsInTextLocker.writeLock().unlock();
//            termsInText.clear();

        }
//        else
//        {
//            termsInText = new HashMap<>();
//            numOfParsedDocInIterative = 0;
//        }

    }

    private String getName() {
        return parseName;
    }

    /**
     * Creates a String that contains all the stopwords from the file <b>resources/stopWords.txt</b>
     */
    protected void createStopWords() {
        if (stopWords == null) {
            stopWords = new HashSet<>();
            File stopWordsFile = new File("./src/main/resources/stopWords.txt");
            if (!stopWordsFile.exists()) {
                System.out.println(stopWordsFile.getAbsolutePath());
            }

            try {
                BufferedReader stopWordsReader = new BufferedReader(new FileReader(stopWordsFile));

                String word = stopWordsReader.readLine();
                while (word != null) {
                    stopWords.add(word.toLowerCase());
                    stopWords.add(word);
                    stopWords.add(word.toUpperCase());
                    word = stopWordsReader.readLine();
                }

                stopWordsReader.close();
//            this.stopWords = (List<String>) Fileo.readObject();

//            Filer.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void parse();





    protected boolean checkTermExist(Term term)
    {
        return toknizr.getTokenList().containsKey(term);
    }


    protected void splitDocText(Document d)
    {
        if (d != null)
        {
           docText = d.getDocText().text().split(" ");
        }
        else
        {
            docText = null;
        }
    }


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

    protected boolean isLastCharPunctuation(String word) {
        if(word == null||word.length() == 0)
        {
            return false;
        }

        for (char punc :
                punctuations) {
            if(word.length()> 0 && word.charAt(word.length()-1) == punc)
            {
                return true;
            }
        }
        return false;
    }

    protected String chopDownFisrtChar(String word) {
        char[] punctuations = {',','.',';',':','?','|','('};

        if(word != null && word.length() >= 2)
        {
            while(isFirstCharPunctuation(word)){
                word = word.substring(1);
            }
        }
        return word;
    }

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

    protected boolean isFraction(String word){
        boolean isFraction = false;

        if(word.length()> 2 && NumberUtils.isNumber(Character.toString(word.charAt(0))) && NumberUtils.isNumber(Character.toString(word.charAt(2))) &&
                Character.toString(word.charAt(1)).equals("/")){
            isFraction = true;
        }

        return isFraction;
    }

    protected double fractionToDecimal(String word){
        double num1 = Double.parseDouble(word.substring(0,0));
        double num2 = Double.parseDouble(word.substring(1,1));
        double fraction = num1/num2;
        double fractionValue = (double) (fraction * 10);
        double decimal = fractionValue % 10;
        double value = decimal * 0.1;
        return value;
    }

    public String[] getDocText() {
        return docText;
    }

    public void clearDic() {
        this.termsInText.clear();
    }



    /**
     * Gets a parsed number and inserting it to the Dictionary
     * @param term
     */
    protected void parsedTermInsert(String term, String currentDocNo)
    {
        termsInTextLocker.readLock().lock();
        if (termsInText.containsKey(term)) {

//            int tf = Integer.parseInt(numbersInText.get(parsedNum).split(",")[1]);
             String docList = termsInText.get(term);
            String[] docsSplitted =  docList.toString().split(";");
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
                lastDocList.append(docAndtf[0]).append(tfDelim).append(oldtf).append(";");
            }
            if(!docAlreadyParsed)
            {
                lastDocList.append(currentDocNo).append(tfDelim).append("1;");
            }
            lastDocList = new StringBuilder(lastDocList.substring(0,lastDocList.length()-1));

            termsInText.replace(term,docList.toString(),lastDocList.toString());

        } else {
            termsInText.put(term, currentDocNo + tfDelim + "1");
        }
        termsInTextLocker.readLock().unlock();
    }

    /**
     * @return the Dictionary of this parser
     */
    public HashMap<String, String> getCopyOfTermInText() {
        return new HashMap<String,String>(termsInText);
    }

    public int qSize()
    {
        return this.docQueueWaitingForParse.size();
    }
}
