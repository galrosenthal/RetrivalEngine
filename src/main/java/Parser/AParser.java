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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AParser implements Runnable {

    protected final double BILLION = 1000000000;
    protected final double MILLION = 1000000;
    protected final double THOUSAND = 1000;
    protected char[] punctuations = {',','.',';',':','?','(',')','"','{','}','-',']','[','!','\t','\n','|','*'};
    private String tfDelim = "#";
    protected String parseName;
    protected String[] docText;
    protected Tokenizer toknizr = Tokenizer.getInstance();
    protected static HashSet<String> stopWords;
    protected static HashSet<String> stopMWords;
//    protected ConcurrentHashMap<String,String> termsInText;
    protected HashMap<String,String> termsInText;
    protected static ConcurrentLinkedQueue<Document> docQueueWaitingForParse;
    protected static int numOfParsedDocInIterative;
    private Indexer myIndexer = Indexer.getInstance();
    private static final int numberOfDocsToPost = 100;
    private static final int numberOfDocsToPost = 500;
    protected volatile boolean stopThread = false;
    protected ReadWriteTempDic myReadWriter = ReadWriteTempDic.getInstance();
    private boolean doneReadingDocs;
    public StringBuilder lastDocList;
    private static ReadWriteLock docEnqDeqLocker = new ReentrantReadWriteLock();
    protected static Semaphore termsInTextSemaphore = new Semaphore(1);
    protected boolean isParsing = false;

//    public static ReadWriteLock termsInTextLock = new ReentrantReadWriteLock();


    protected AParser()
    {
//        termsInText = new ConcurrentHashMap<>();
        termsInText = new HashMap<>();
        docQueueWaitingForParse = new ConcurrentLinkedQueue<>();
        numOfParsedDocInIterative = 0;
        createStopWords();
        createMStopWords();
        doneReadingDocs = false;
        stopThread = false;



    }

    public void stopThread()
    {
        doneReadingDocs = true;
        while(isParsing);
        releaseToIndexerFile();
        stopThread = true;

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


    protected void releaseToIndexerFile()
    {
        if(numOfParsedDocInIterative >= numberOfDocsToPost || doneReadingDocs)
        {
//            if(!myReadWriter.writeToDic(termsInText,getName()))
            termsInTextSemaphore.acquireUninterruptibly();
            System.out.println("Parsed " + termsInText.size() + " terms");
//            termsInTextLocker.writeLock().lock();
            if(!Indexer.getInstance().enqueue(termsInText))
            {
                System.out.println("Fuck it");
                //TODO: maybe throw exception?
            }
//            myIndexer.enqueue(termsInText);
//            termsInText = null;
//            termsInText = new ConcurrentHashMap<>();
            termsInText = new HashMap<>();
//            termsInTextLocker.writeLock().unlock();
            numOfParsedDocInIterative = 0;
            termsInTextSemaphore.release();

//            termsInText.clear();

        }

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

    protected void createMStopWords() {
        if (stopMWords == null) {
            stopMWords = new HashSet<>();
            File stopWordsFile = new File("./src/main/resources/moreStopWords.txt");
            if (!stopWordsFile.exists()) {
                System.out.println(stopWordsFile.getAbsolutePath());
            }

            try {
                BufferedReader stopWordsReader = new BufferedReader(new FileReader(stopWordsFile));

                String word = stopWordsReader.readLine();
                while (word != null) {
                    stopMWords.add(word.toLowerCase());
                    stopMWords.add(word);
                    stopMWords.add(word.toUpperCase());
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

    protected StringBuilder chopDownLastCharPunc(StringBuilder word) {

        if(word != null && word.length() >= 1)
        {
//            word = word.toLowerCase();
            while(isLastCharPunctuation(word))
            {
                word =new StringBuilder(word.substring(0,word.length()-1));
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

    protected boolean isLastCharPunctuation(StringBuilder word) {
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

    protected StringBuilder chopDownFisrtChar(StringBuilder word) {
        char[] punctuations = {',','.',';',':','?','|','('};

        if(word != null && word.length() >= 2)
        {
            while(isFirstCharPunctuation(word)){
                word =new StringBuilder( word.substring(1));
            }
        }
        return word;
    }

    protected  boolean isFirstCharPunctuation(StringBuilder word) {
        if(word != null && word.length() >= 2)
        {
            //word = word.toLowerCase();
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
        String[] splittedFraction = word.split("/");
//        wordForSplitting.split(" ");

        if(splittedFraction.length == 2 && NumberUtils.isNumber(splittedFraction[0]) && NumberUtils.isNumber(splittedFraction[1]))
        {
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

    protected void parsedTermInsert(String term, String currentDocNo,String parserName) {
        if(term.equalsIgnoreCase(""))
        {
            System.out.println("Term is Empty " + parserName);
        }
        parsedTermInsert(term,currentDocNo);

    }


    /**
     * Gets a parsed number and inserting it to the Dictionary
     * @param term
     */
    protected void parsedTermInsert(String term, String currentDocNo) {
//        termsInTextLocker.readLock().lock();
//        termsInTextSemaphore.acquireUninterruptibly();

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
                lastDocList.append(docAndtf[0] + tfDelim + oldtf + ";");
            }
            if(!docAlreadyParsed)
            {
                lastDocList.append(currentDocNo + tfDelim + "1;");
            }
            lastDocList = new StringBuilder(lastDocList.substring(0,lastDocList.length()-1));

            termsInText.replace(term,docList.toString(),lastDocList.toString());

        } else {
            termsInText.put(term, currentDocNo + tfDelim + "1");
        }
//        termsInTextLocker.readLock().unlock();
//        termsInTextSemaphore.release();
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
