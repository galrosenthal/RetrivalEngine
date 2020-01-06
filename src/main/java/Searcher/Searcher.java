package Searcher;

import IR.Document;
import Indexer.Indexer;
import Parser.AParser;
import Parser.MainParse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Searcher {
    String corpusPath;

    public Searcher(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public List<String> searchQuery(IR.Document query, boolean withSemantic){
        String corpusPathAndLineDelim = "#";
        Path termFilePathTemp;
        Indexer myIndexer = Indexer.getInstance();
        HashMap<String,String> corpusDictionary = myIndexer.getCorpusDictionary();
        if(corpusDictionary!=null){
            AParser parser = new MainParse();
            parser.setPathToCorpus(corpusPath);
            ((MainParse) parser).parse(query);
            
            HashMap<String,String> termInText = parser.getTermsInText();
            HashMap<String,String> termswithPosting = new HashMap<>();

            for (Map.Entry<String,String> term: termInText.entrySet()) {
                ArrayList<String> allTermsOfLetter = new ArrayList<>();
                String specificTermKey = term.getKey();
                String valueFromCorpus = corpusDictionary.get(specificTermKey);
                if (valueFromCorpus != null) {
                    String[] splittedValue = valueFromCorpus.split(corpusPathAndLineDelim);
                    termFilePathTemp = Paths.get(splittedValue[0]);
                    try {
                        allTermsOfLetter = (ArrayList<String>) Files.readAllLines(termFilePathTemp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int lineNumberInFile = Integer.parseInt(corpusDictionary.get(specificTermKey).split(corpusPathAndLineDelim)[1]);
                    termswithPosting.put(specificTermKey, allTermsOfLetter.get(lineNumberInFile-1));
                    //System.out.println(allTermsOfLetter.get(lineNumberInFile-1));
                }
            }

            if(withSemantic){
                System.out.println("with semantic");
            }
        }

        List<String> result = new ArrayList<>();
        result= Arrays.asList("doc1","doc2","doc3");

        return result;
    }
}
