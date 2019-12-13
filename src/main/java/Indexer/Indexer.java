package Indexer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer implements Runnable{
    private static final double MAX_POSTING_FILE_SIZE = 5;
    private static final int MAX_TERMS_TO_INDEX = 100000;
    private int countMergedTerms = 0;
    private static volatile Indexer mInstance;
    private final int KB_SIZE = 1024;
    //    private ConcurrentLinkedQueue<ConcurrentHashMap<String,String>> parsedWordsQueue;
    private ConcurrentLinkedQueue<HashMap<String,String>> parsedWordsQueue;
    private String postFiles;
    private BufferedWriter fileWriter;
    public static volatile boolean stopThreads = false;
    public ConcurrentHashMap<String,String> corpusDictionary;
    private String indexerName = "Indexer ";
    private static AtomicInteger indexerNum;
    private String pathToPostFolder="./postingFiles/";
    public HashMap<String,String> hundredKtermsMap;

    private Indexer() {
        this.parsedWordsQueue = new ConcurrentLinkedQueue<>();
        corpusDictionary = new ConcurrentHashMap<>();
        hundredKtermsMap = new HashMap<>();

    }
    public boolean isQEmpty()
    {
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


    public synchronized boolean enqueue(HashMap<String,String> parsedWords)
    {
        return parsedWordsQueue.add(parsedWords);
    }

    private synchronized HashMap<String,String> dequeue()
    {
        return parsedWordsQueue.poll();
    }

    public void setPathToPostFiles(String path)
    {
        this.postFiles = path;
    }


    @Override
    public void run() {
//        System.out.println("Indexer has Started...");
        while(!stopThreads)
        {
            createPostFiles();
        }
//        System.out.println("Indexer has stopped...");
//        createPostFiles();
        System.out.println("Corpus Dictionary size is: " + corpusDictionary.keySet().size());

    }

    /*private void createPostFiles() {

        //TODO: For each word check if exists in the CorpusDictionary,
        // find the relevant posting file (from the Dictionary or by first letter),
        // append the relevant data to the posting file in the relevant line

        //map2.forEach(
        //    (key, value) -> map1.merge( key, value, (v1, v2) -> v1 + ";" + v2)
        //);
//        HashMap<String,String> dqdHshMap = ReadWriteTempDic.getInstance().readFromDic();
        HashMap<String,String> dqdHshMap = dequeue();



        if(dqdHshMap == null)
        {
//            System.out.println("Could not read Object from File");
            return;
        }
        else
        {
//                System.out.println("cleared " + dqdHshMap.size());
            for (String term :
                    dqdHshMap.keySet()) {
                if(!corpusDictionary.containsKey(term))
                {
                    String dfList = dqdHshMap.get(term);
//                    String[] splittedDocs = dfList.split(";");
                    try {
                        if(term.length()>0 && (term.charAt(0) == ' ' || term.charAt(0) == '/' || term.charAt(0) == '.'))
                        {
                            term = term.substring(1);
                        }
                        if(term.equals(""))
                        {
                            throw new Exception("Term Empty before creating new file");
                        }
                        String lineIndexInFile = createAndWriteTheFile(term.toLowerCase().charAt(0), dfList);
                        if (lineIndexInFile == null) {
                            throw new Exception("Could Not Write The file properly");
                        }
                        this.corpusDictionary.put(term, lineIndexInFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    //The corpus already contains this term
                    String[] postFileAndLine = corpusDictionary.get(term).split("#");
                    readAndAppendToFile(term,postFileAndLine[0],postFileAndLine[1],dqdHshMap.get(term));


                }

            }

            dqdHshMap.clear();
            dqdHshMap = null;

            //System.out.println("test");
        }

//            for (String term :
//                    dqdHshMap.keySet()) {
//
//
//            }


    }*/

    private void createPostFiles()
    {
        //map2.forEach(
        //    (key, value) -> map1.merge( key, value, (v1, v2) -> v1 + ";" + v2)
        //);
//        HashMap<String,String> dqdHshMap = ReadWriteTempDic.getInstance().readFromDic();
        while(!isQEmpty()) {
//            System.out.println("There are " + parsedWordsQueue.size() + " Maps left in the Q");
            HashMap<String, String> dqdHshMap = dequeue();

            if (dqdHshMap == null) {
                continue;
            }
            long startTime,endTime;


//            System.out.println("Merging "+dqdHshMap.size());
            int mapSizeBeforeMerge = hundredKtermsMap.size();
            startTime = System.nanoTime();
            mergeHashMapIntoHundred(dqdHshMap);
            endTime = System.nanoTime();
            int mapSizeAfterMerge = hundredKtermsMap.size();
            countMergedTerms += (mapSizeAfterMerge-mapSizeBeforeMerge);
//            System.out.println("Merging took "+(endTime - startTime)/1000000000 + " seconds");

            if (countMergedTerms >= MAX_TERMS_TO_INDEX) {
//                System.out.println("Sorting "+hundredKtermsMap.size());
//                startTime = System.nanoTime();
                sortDocListPerTerm();
//                endTime = System.nanoTime();
                countMergedTerms = 0;
//                System.out.println("Sorting took "+(endTime - startTime)/1000000000 + " seconds");
            }

        }

//        System.out.println("Indexer Q is empty");
//        try {
//            Thread.sleep(500);
//        }
//        catch (Exception e)
//        {
//
//        }
//        indexTheFucker();



    }

    public void sortDocListPerTerm() {
//        String[] myString = {"FBIS3-8#6","FBIS3-1#2","FBIS3-7#32","FBIS3-2#43","FBIS3-4#54","FBIS3-3#5","FBIS3-5#98","FBIS3-6#12"};
//        Arrays.sort(myString);
//        System.out.println(Arrays.toString(myString));
//        StringBuilder newString = new StringBuilder(Arrays.toString(myString));

        for (String key :
                hundredKtermsMap.keySet()) {
            String[] docList = hundredKtermsMap.get(key).split(";");
            Arrays.sort(docList);
            String sortedList = Arrays.toString(docList);
            if(sortedList.charAt(0) == '[')
            {
                sortedList = sortedList.substring(1);
            }
            if(sortedList.charAt(sortedList.length()-1) == ']')
            {
                sortedList = sortedList.substring(0,sortedList.length()-1);
            }

            hundredKtermsMap.replace(key,hundredKtermsMap.get(key),sortedList);
        }
    }

    private void mergeHashMapIntoHundred(HashMap<String, String> hashMapToMerge) {
        hashMapToMerge.forEach(
            (key, value) -> hundredKtermsMap.merge( key, value, (v1, v2) -> v1 + ";" + v2)
        );
    }


    // read file one line at a time
// replace line as you read the file and store updated lines in StringBuffer
// overwrite the file with the new lines
    public synchronized void readAndAppendToFile(String term,String fileNum,String lineIndex,String parsedData) {
        try {
            // input the (modified) file content to the StringBuffer "input"
            String pathToFileForEdit = pathToPostFolder + term.toLowerCase().charAt(0) +"/" +fileNum;
            BufferedReader file = new BufferedReader(new FileReader(pathToFileForEdit));
            StringBuffer inputBuffer = new StringBuffer();
            String line;
            int lineIndexCounter = 0;
            int parsedLineIndex = Integer.parseInt(lineIndex);


            while ((line = file.readLine()) != null) {
                lineIndexCounter++;
                if(lineIndexCounter == parsedLineIndex)
                {
                    line = line +";" + parsedData;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(pathToFileForEdit);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("Problem reading file.");
        }
    }
    private synchronized String createAndWriteTheFile(char firstLetterForFolderName, String splittedDocs) throws Exception{

//        int currIndex = writeIndex.getAndIncrement();
        boolean finishedWriting = false;
        String newFilePath = pathToPostFolder + firstLetterForFolderName;
        String fileNumAndLineIndex = "";
//        System.out.println("Posting File Path: " + newFilePath);
        Path pathForNewFile = null;
        try {
            pathForNewFile = Paths.get(newFilePath);

            if(!Files.exists(pathForNewFile))
            {
                Path a = Files.createDirectories(pathForNewFile);
//            System.out.println(a.getFileName().toString());
            }

            //System.out.println(pathForNewFile.toString());

            int indexFile = getIndexInFolder(pathForNewFile);
            if(indexFile == -1)
            {
                throw new Exception("Could not find Dir");
            }
            newFilePath += "/"+indexFile;
            pathForNewFile = Paths.get(newFilePath);

            try
            {

                CharSequence fromStr = new StringBuffer(splittedDocs);
//            double fileSize = getFileSizeMegaBytes(pathForNewFile.toFile());

                BufferedWriter writeToPostFile = new BufferedWriter(new FileWriter(pathForNewFile.toFile()));
                writeToPostFile.append(fromStr);
                writeToPostFile.flush();
                writeToPostFile.close();
                finishedWriting = true;

            }
            catch (Exception e)
            {
                System.out.println("Could not load file");
            }

            if(finishedWriting)
            {
                List<String> linesInFile = Files.readAllLines(pathForNewFile);
                fileNumAndLineIndex = pathForNewFile.getFileName().toString() + "#" + linesInFile.size();
                return fileNumAndLineIndex;
            }
        }
        catch (Exception e)
        {
            System.out.println("Could not Parse the path: "+ newFilePath);
        }
        return null;

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
                    if (getFileSizeMegaBytes(child) <= MAX_POSTING_FILE_SIZE)
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
        return (double) file.length() / (KB_SIZE * KB_SIZE);
    }
}
