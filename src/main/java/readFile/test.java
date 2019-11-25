package readFile;

import java.io.File;

public class test {

    public static void main(String[] args) {
        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpus";
        ReadFile f = new ReadFile();
        File corpus = new File(path);
        long startTime,endTime;
        startTime = System.nanoTime();
        f.readCorpus(corpus);
        endTime = System.nanoTime();

        System.out.println("There are "+f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
    }
}
