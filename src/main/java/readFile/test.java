package readFile;

import Indexer.Indexer;

import java.io.File;

public class test {

    public static void main(String[] args) {
        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpus";
        String test = "-1010.56";
//        test = test.substring(0,test.indexOf("."));
        test = test.replaceAll(",","");
        Double testValue = Double.valueOf(test);


        Indexer.getInstance().setPathToPostFiles(path);


        ReadFile f = new ReadFile();
        File corpus = new File(path);
        long startTime,endTime;
        startTime = System.nanoTime();
        f.readCorpus(corpus);
        endTime = System.nanoTime();
        //HashMap<String,Integer> testNumInAllCorpus = f.prsNums.getNumbersInText();


        System.out.println("There are "+f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
        System.out.println("Num of terms: " + f.getNumOfTerms());
    }
}
