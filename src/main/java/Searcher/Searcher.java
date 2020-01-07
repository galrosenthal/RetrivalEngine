package Searcher;

import datamuse.*;
import Indexer.Indexer;
import Parser.AParser;
import Parser.MainParse;
import Ranker.Ranker;
import java.io.IOException;
import java.lang.reflect.Array;
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
        Ranker ranker = Ranker.getInstance();
        String corpusPathAndLineDelim = "#";
        Path termFilePathTemp;
        Indexer myIndexer = Indexer.getInstance();
        List<String> result = new ArrayList<>();

        HashMap<String,String> corpusDictionary = myIndexer.getCorpusDictionary();
        if(corpusDictionary!=null){

            AParser parser = new MainParse();
            parser.setPathToCorpus(corpusPath);
            ((MainParse) parser).parse(query);
            
            HashMap<String,String> termInText = parser.getTermsInText();
            HashMap<String,String> termswithPosting = new HashMap<>();

            if(withSemantic){
                DatamuseQuery dQuery = new DatamuseQuery();

                for (String term: termInText.keySet()) {
                    String s = dQuery.findSimilar(term);
                    String[] res = JSONParse.parseWords(s);
                    for (String word:res) {
                        termInText.put(word,"1");
                    }
                }
            }

            for (String term: termInText.keySet()) {
                ArrayList<String> allTermsOfLetter = new ArrayList<>();
                String specificTermKey = term;
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
            result =ranker.rankQueryDocs(termswithPosting,termInText);
        }

       // result= Arrays.asList("doc1","doc2","doc3");

        return result;
    }
}
