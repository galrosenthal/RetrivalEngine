package readFile;

import java.io.File;

public class test {

    public static void main(String[] args) {
        String path = "C:\\Users\\orans\\Documents\\University\\Third year\\Semester E\\Information Retrieval\\corpus";
        ReadFile f = new ReadFile();
        File corpus = new File(path);
        f.readCorpus(corpus);
    }
}
