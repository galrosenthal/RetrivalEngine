package Searcher;

import com.medallia.word2vec.Word2VecModel;
import com.medallia.word2vec.util.Common;
import datamuse.*;
import Indexer.Indexer;
import Parser.AParser;
import Parser.MainParse;
import Ranker.Ranker;

import java.io.File;
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
            System.out.println("Finishing parsing query: " +query.getDocNo());
            
            HashMap<String,String> termInText = parser.getTermsInText();
            //((MainParse) parser).parse(query);
            HashMap<String,String> descTest = parser.getTermsInText();
            HashMap<String,String> termswithPosting = new HashMap<>();

            if(withSemantic){
                /**
                 * Datamuse API
                 */
                DatamuseQuery dQuery = new DatamuseQuery();

                //for (String term: termInText.keySet()) {
                //    String s = dQuery.findSimilar(term);
               //     String[] res = JSONParse.parseWords(s);
              //      for (String word:res) {
              //          termInText.put(word,"1");
              //      }
             //   }

                /**
                 * Word2Vec model
                 */
                final String filename = "word2vec.c.output.model.txt";
                try {
                    Word2VecModel model = Word2VecModel.fromTextFile(new File(filename));
                    List<com.medallia.word2vec.Searcher.Match> matches = model.forSearch().getMatches("internal", 5);
                    for (com.medallia.word2vec.Searcher.Match match:matches) {
                        termInText.put(match.match(),"1");
                    }

                } catch (IOException | com.medallia.word2vec.Searcher.UnknownWordException e) {
                    e.printStackTrace();
                }
            }


            for (String specificTermKey: termInText.keySet()) {
                ArrayList<String> allTermsOfLetter = new ArrayList<>();

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
            System.out.println("Finishing posting query: " +query.getDocNo());
            result =ranker.rankQueryDocs(termswithPosting,termInText);
            System.out.println("Finishing ranking query: " +query.getDocNo());
        }

       // result= Arrays.asList("doc1","doc2","doc3");

        return result;
    }
}
