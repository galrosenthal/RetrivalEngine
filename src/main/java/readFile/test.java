package readFile;

import java.io.File;

public class test {

    public static void main(String[] args) {
        String path = "C:\\Users\\Gal\\Documents\\Stduies\\Third Year\\Semester A\\corpus";
//        String path = "C:\\Users\\Gal\\Documents\\corpusCopy";


        String test = "-1010.56";
//        test = test.substring(0,test.indexOf("."));
        test = test.replaceAll(",","");


        Double testValue = Double.valueOf(test);




        ReadFile f = new ReadFile();
        File corpus = new File(path);
        long startTime,endTime;
        startTime = System.nanoTime();
        f.readCorpus(corpus);
        endTime = System.nanoTime();


        System.out.println("There are "+f.numOfCorpusFiles + " files in the corpus and it took: " + (endTime - startTime)/1000000000 + " Seconds to iterate over them all");
    }
}
