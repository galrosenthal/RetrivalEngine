package Indexer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer implements Runnable {
    private static final double MAX_POSTING_FILE_SIZE = Double.MAX_VALUE;
    private static final int MAX_TERMS_TO_INDEX = 500000;
    private int countMergedTerms = 0;
    private static volatile Indexer mInstance;
    private final int KB_SIZE = 1024;
    //    private ConcurrentLinkedQueue<ConcurrentHashMap<String,String>> parsedWordsQueue;
    private ConcurrentLinkedQueue<HashMap<String, String>> parsedWordsQueue;
    private BufferedWriter fileWriter;
    public static volatile boolean stopThreads = false;
    //    public ConcurrentHashMap<String,String> corpusDictionary;
    public HashMap<String, String> corpusDictionary;
    private String indexerName = "Indexer ";
    private static AtomicInteger indexerNum;
    private String pathToPostFolder = "./postingFiles/";
    private String pathToTempFolder = "./dicTemp/";
    public HashMap<String, String> hundredKtermsMap;

    private Indexer() {
        this.parsedWordsQueue = new ConcurrentLinkedQueue<>();
        corpusDictionary = new HashMap<>();
        hundredKtermsMap = new HashMap<>();
        indexerNum = new AtomicInteger(0);

    }

    public boolean isQEmpty() {
        return parsedWordsQueue.isEmpty();
    }

    public static Indexer getInstance() {
        if (mInstance == null) {
            synchronized (Indexer.class) {
                if (mInstance == null) {
                    mInstance = new Indexer();
                }
            }
        }
        return mInstance;
    }


    public synchronized boolean enqueue(HashMap<String, String> parsedWords) {
        return parsedWordsQueue.add(parsedWords);
    }

    private synchronized HashMap<String, String> dequeue() {
        return parsedWordsQueue.poll();
    }

    public void setPathToPostFiles(String path) {

        this.pathToPostFolder = path;
    }


    @Override
    public void run() {
//        System.out.println("Indexer has Started...");
        while (!stopThreads) {
            createPostFiles();
        }
        writeHashMapToDisk();
//        System.out.println("Indexer has stopped...");
//        createPostFiles();
        System.out.println("Corpus Dictionary size is: " + corpusDictionary.keySet().size());

    }

    public int corpusSize() {
        return corpusDictionary.size();
    }


    private void createPostFiles() {

        while (!isQEmpty()) {
//            System.out.println("There are " + parsedWordsQueue.size() + " Maps left in the Q");
            HashMap<String, String> dqdHshMap = dequeue();

            if (dqdHshMap == null) {
                continue;
            }
            long startTime, endTime;


//            System.out.println("Merging "+dqdHshMap.size());
            int mapSizeBeforeMerge = hundredKtermsMap.size();
            startTime = System.nanoTime();
            mergeHashMapIntoHundred(dqdHshMap, hundredKtermsMap);
            endTime = System.nanoTime();
            int mapSizeAfterMerge = hundredKtermsMap.size();
            countMergedTerms += (mapSizeAfterMerge - mapSizeBeforeMerge);
//            System.out.println("Merging took "+(endTime - startTime)/1000000000 + " seconds");

            if (countMergedTerms >= MAX_TERMS_TO_INDEX) {
//                System.out.println("Sorting "+hundredKtermsMap.size());
//                startTime = System.nanoTime();
//                sortDocListPerTerm();
                writeHashMapToDisk();
//                endTime = System.nanoTime();
                countMergedTerms = 0;
//                System.out.println("Sorting took "+(endTime - startTime)/1000000000 + " seconds");
            }

        }
    }

    /**
     * Writes the temp Dictionary sorted to the disk as object
     */
    public void writeHashMapToDisk() {
        sortDocListPerTerm();
        try {
            if(!Paths.get(pathToTempFolder).toFile().exists())
            {
                Files.createDirectories(Paths.get(pathToTempFolder));
            }
            FileOutputStream fileOut = new FileOutputStream(pathToTempFolder + indexerNum.getAndIncrement(), true);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(hundredKtermsMap);
            hundredKtermsMap = new HashMap<>();
            objectOut.flush();
            objectOut.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sorts the HashMap value if is as expected,
     * DocList String:
     * "FBIS3-8#6;FBIS3-1#2;BIS3-7#32;FBIS3-2#43;FBIS3-4#54;FBIS3-3#5;FBIS3-5#98;FBIS3-6#12"
     * will be:
     * "FBIS3-1#2;FBIS3-2#43;FBIS3-3#5;FBIS3-4#54;FBIS3-5#98;FBIS3-6#12;BIS3-7#32;FBIS3-8#6"
     */
    private void sortDocListPerTerm() {
//        String[] myString = {"FBIS3-8#6","FBIS3-1#2","FBIS3-7#32","FBIS3-2#43","FBIS3-4#54","FBIS3-3#5","FBIS3-5#98","FBIS3-6#12"};
//        Arrays.sort(myString);
//        System.out.println(Arrays.toString(myString));
//        StringBuilder newString = new StringBuilder(Arrays.toString(myString));

        for (String key :
                hundredKtermsMap.keySet()) {
            String[] docList = hundredKtermsMap.get(key).split(";");
            String sortedList = sortArray(docList);

            hundredKtermsMap.replace(key, hundredKtermsMap.get(key), sortedList);
        }
    }

    /**
     * Gets an Array of doc List and sorts it alphabetically
     * @param docList - An Array of docList
     * @return String containing the docList sorted
     */
    private String sortArray(String[] docList) {
        long start, end;
        start = System.nanoTime();
        Arrays.sort(docList);
        end = System.nanoTime();
//            System.out.println("The Sort took " + (end-start)/1000000 + " Milli Seconds");
        String sortedList = Arrays.toString(docList).replaceAll(",", ";");
//        sortedList = Arrays.toString(docList).replaceAll(" ", "");
        if (sortedList.charAt(0) == '[') {
            sortedList = sortedList.substring(1);
        }
        if (sortedList.charAt(sortedList.length() - 1) == ']') {
            sortedList = sortedList.substring(0, sortedList.length() - 1);
        }
        return sortedList;
    }

    /**
     * Gets 2 HashMaps
     * and merges equal term and theris values
     *
     * @param hashMapToMergeFrom - the HashMap its value you want to merge
     * @param hashMapToMergeTo   - the HashMap you want to merge the terms into it
     */
    private void mergeHashMapIntoHundred(HashMap<String, String> hashMapToMergeFrom, HashMap<String, String> hashMapToMergeTo) {
        hashMapToMergeFrom.forEach(
                (key, value) -> hashMapToMergeTo.merge(key, value, (v1, v2) -> v1 + ";" + v2)
        );
    }


    /**
     * Creates the Dictionary and postfiles from the saved Temp Dictionaries
     */
    public void createCorpusDictionary() {
        indexerNum = new AtomicInteger(0);


        try {

            while (Objects.requireNonNull(Paths.get(pathToTempFolder).toFile().listFiles()).length > 0) {
                /**Read objects**/
                File hashMapFile = Paths.get(pathToTempFolder + indexerNum.getAndIncrement()).toFile();
                FileInputStream fileIn = new FileInputStream(hashMapFile);
                ObjectInputStream objectOut = new ObjectInputStream(fileIn);
                HashMap<String, String> newMap = (HashMap<String, String>) objectOut.readObject();
                objectOut.close();
                fileIn.close();
                hashMapFile.delete();
                System.out.println("Read HashMap with size: " + ((HashMap) newMap).size());
                mergeReadMapIntoCorpus(newMap);
//                mergeHashMapIntoHundred(newMap,corpusDictionary);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    /**
     * Gets the Read HashMap from Object file
     * And Merging it into the Dictionary while writing the post files.
     * @param newMap - the HashMap Read from the Object File
     */
    private void mergeReadMapIntoCorpus(HashMap<String,String> newMap)
    {

        ArrayList<String> sortedKeys = new ArrayList<String>(newMap.keySet());
        Collections.sort(sortedKeys);
        System.out.println("Sorted " + sortedKeys.size() + " HashMap keys");
        int i = 0;
        //char docLetter = sortedKeys.get(0).toLowerCase().charAt(i++);
//        while(docLetter == ' ')
//        {
//            docLetter = sortedKeys.get(0).toLowerCase().charAt(i++);
//        }
//        while(sortedKeys.get(0).equalsIgnoreCase(""))
//        {
//            sortedKeys.remove(0);
//        }
        int termIndexInSortedKeys = 0;
        String corpusPathAndLineDelim = "#";
        Path termFilePath = getFileForTerm(sortedKeys.get(0));
        List<String> allTermsOfLetter = new ArrayList<>();
        if(termFilePath.toFile().exists())
        {
            try{
                allTermsOfLetter = Files.readAllLines(termFilePath);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        for (String termKey :
                sortedKeys){

            Path termFilePathTemp;
            if(corpusDictionary.containsKey(termKey.toLowerCase()))
            {
                String valueFromCorpus = corpusDictionary.get(termKey.toLowerCase());
                String[] splittedValue = valueFromCorpus.split(corpusPathAndLineDelim);
                termFilePathTemp = Paths.get(splittedValue[0]);
            }
            else
            {
                termFilePathTemp = getFileForTerm(termKey);
            }

            if (!(termFilePathTemp.toString().equalsIgnoreCase(termFilePath.toString()))) {
                //the new termFile is not the same as last one
                writePostFileOfLetter(termFilePath,allTermsOfLetter);
                termFilePath = termFilePathTemp;
                if (termFilePath.toFile().exists()) {
                    try {

                        System.out.println("Reading all lines from file: " + termFilePath.toFile().getPath());
//                        allTermsOfLetter = new ArrayList<>();
                        allTermsOfLetter = Files.readAllLines(termFilePath);
                        termFilePath.toFile().delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    allTermsOfLetter = new ArrayList<>();
                }
            }

            if (corpusDictionary.containsKey(termKey.toLowerCase()))
            {

                int lineNumberInFile = Integer.parseInt(corpusDictionary.get(termKey.toLowerCase()).split(corpusPathAndLineDelim)[1]);
                StringBuilder addNewHashMapLineToExisting = new StringBuilder();
                addNewHashMapLineToExisting.append(allTermsOfLetter.get(lineNumberInFile-1)).append(";").append(newMap.get(termKey));
                //Gets and set new Total TF
                int totalTF = sumTotalTF(termKey,newMap.get(termKey),corpusPathAndLineDelim);
                String pathAndLineAndTTF = corpusDictionary.get(termKey.toLowerCase());
                String[] pathLineTTF = pathAndLineAndTTF.split(corpusPathAndLineDelim);
                pathAndLineAndTTF = pathLineTTF[0] + corpusPathAndLineDelim + pathLineTTF[1] + corpusPathAndLineDelim + totalTF;
                corpusDictionary.replace(termKey.toLowerCase(),corpusDictionary.get(termKey.toLowerCase()),pathAndLineAndTTF);
                //sort and set the line in file with the new values
                String newLineSorted = sortArray(addNewHashMapLineToExisting.toString().split(";"));
                allTermsOfLetter.set(lineNumberInFile-1,newLineSorted);

//                termFilePath = Paths.get(corpusDictionary.get(termKey.toLowerCase()).split(corpusPathAndLineDelim)[0]);
            }
            else
            {
//                termFilePath = getFileForTerm(termKey);
                allTermsOfLetter.add(newMap.get(termKey));
                String pathAndLine = termFilePath.toString() + corpusPathAndLineDelim + allTermsOfLetter.size() + corpusPathAndLineDelim + sumTotalTF(termKey,newMap.get(termKey),corpusPathAndLineDelim);
                corpusDictionary.put(termKey.toLowerCase(),pathAndLine);
            }
        }
        if(allTermsOfLetter.size() != 0)
        {
            writePostFileOfLetter(termFilePath,allTermsOfLetter);
        }

    }

    private int sumTotalTF(String termKey, String docList,String delim) {
        String[] docListSplitted = docList.split(";");
        int sum;
        if(corpusDictionary.containsKey(termKey.toLowerCase()))
        {
            sum = Integer.parseInt(corpusDictionary.get(termKey.toLowerCase()).split(delim)[2]);
        }
        else
        {
            sum = 0;
        }
        int tfInSpecificDoc = 0;
        for (String doc :
                docListSplitted) {
            String[] docParams = doc.split(delim);
            tfInSpecificDoc = Integer.parseInt(docParams[1]);
            sum += tfInSpecificDoc;
        }

        return sum;
    }

    /**
     * Gets a term from the hash map
     * and returns the Path to the post file it suppose to be in
     * @param termKey - the term to look for its path
     * @return The path of the post file of termKey
     */
    private Path getFileForTerm(String termKey)
    {

        if (termKey.length() > 0 && (termKey.charAt(0) == ' ' || termKey.charAt(0) == '/' || termKey.charAt(0) == '\'' || termKey.charAt(0) == '.'))
        {
            termKey = termKey.substring(1);
        }
        String pathToTerm = pathToPostFolder+"/"+termKey.toLowerCase().charAt(0);
        Path realPathOfTermFile = Paths.get(pathToTerm);
        if(!realPathOfTermFile.toFile().exists())
        {
            try {
                Files.createDirectories(realPathOfTermFile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        int indexForFile = getIndexInFolder(Paths.get(pathToTerm));
        Path pathToPost = Paths.get( pathToTerm + "/" +indexForFile);
        return pathToPost;
    }

    /**
     * Gets the path to the post file and the list of lines of this file
     * and writes the lines to the file
     *
     * <B><I>If the file already exists it will overwrite it, since the lines will already contains the lines
     * present in the file.</I></B>
     * @param termFilePath - path to the post file
     * @param allTermsOfLetter - lines which will be in the post file
     */
    private void writePostFileOfLetter(Path termFilePath, List<String> allTermsOfLetter)
    {
        try {
            if(!termFilePath.toFile().exists())
            {
                if(!termFilePath.getParent().toFile().exists())
                {
                    Files.createDirectories(termFilePath.getParent());
                }
                Files.createFile(termFilePath);
            }

            BufferedWriter writeBuffer = new BufferedWriter(new FileWriter(termFilePath.toFile(),true));
            for (String line :
                    allTermsOfLetter) {
                writeBuffer.append(line);
                writeBuffer.newLine();
            }
            writeBuffer.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Gets a path to folder of posting files
     * searches for a file with size less than MAX_POSTING_FILE_SIZE MB
     * if it finds it, than this file index is the returned value
     * else it returns the next number for a new File (if there are 5 files in the directory, it returns 6)
     * @param pathToFolder - folder of posting files (a,b,c...etc')
     * @return the index of the file needs to be written
     */
    private synchronized int getIndexInFolder(Path pathToFolder) {
        File dir = pathToFolder.toFile();
        File[] directoryListing = dir.listFiles();
        int indexOfFile = -1, numOfFilesInDir = 0;
        try {
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (getFileSizeMegaBytes(child) < MAX_POSTING_FILE_SIZE)
                    {
                        String indexStr = child.getName();
                        indexOfFile = Integer.parseInt(indexStr);
                    }
                    numOfFilesInDir++;
                }
                if(indexOfFile==-1)
                {
                    indexOfFile=numOfFilesInDir;
                }
            } else {
                // Handle the case where dir is not really a directory.
                // Checking dir.isDirectory() above would not be sufficient
                // to avoid race conditions with another process that deletes
                // directories.
                if(dir.isDirectory())
                {
                    return 0;
                }
                else
                {
                    throw new Exception("There is no Dir");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(pathToFolder.toString());
            e.printStackTrace();
        }

        return indexOfFile;
    }

    private double getFileSizeMegaBytes(File file) {
        double a = (double) file.length() / (KB_SIZE * KB_SIZE);
        return a;
    }


    public void saveCorpusDictionary() {

    }
}
