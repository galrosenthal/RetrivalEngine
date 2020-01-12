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

    public List<String> searchQuery(IR.Document query,IR.Document desc, boolean withSemantic){
        Ranker ranker = Ranker.getInstance();

        Path termFilePathTemp;
        Indexer myIndexer = Indexer.getInstance();
        List<String> result = new ArrayList<>();

        HashMap<String,String> corpusDictionary = myIndexer.getCorpusDictionary();
        if(corpusDictionary!=null){
            HashMap<String, String> descInText;
            AParser parser = new MainParse();
            AParser parser2 = new MainParse();
            parser.setPathToCorpus(corpusPath);
            ((MainParse) parser).parse(query);
            System.out.println("Finishing parsing query: " +query.getDocNo());
            
            HashMap<String,String> termInText = parser.getTermsInText();
            if(desc.getTextArray() != null) {
                ((MainParse) parser2).parse(desc);
                 descInText = parser2.getTermsInText();
            }
            else{
                descInText = new HashMap<>();

            }
            HashMap<String,String> termswithPosting = new HashMap<>();
            HashMap<String,String> termswithPostingDesc = new HashMap<>();

            HashMap<String,String> termswithSemanticInText = new HashMap<>();
            HashMap<String,String> termswithSemanticPosting = new HashMap<>();

            if(withSemantic){
                String[] res;
                /**
                 * Datamuse API
                 */
                DatamuseQuery dQuery = new DatamuseQuery();

                for (String term: termInText.keySet()) {
                    String s = dQuery.findSimilar(term);
                    res = JSONParse.parseWords(s);
                }


                /**
                 * Word2Vec model
                 */
                final String filename = "word2vec.c.output.model.txt";
                try {
                    for (String term: termInText.keySet()) {
                        Word2VecModel model = Word2VecModel.fromTextFile(new File(filename));
                        List<com.medallia.word2vec.Searcher.Match> matches = model.forSearch().getMatches(term, 2);
                        for (com.medallia.word2vec.Searcher.Match match : matches) {
                            
                        }
                    }

                } catch (IOException | com.medallia.word2vec.Searcher.UnknownWordException e) {
                    e.printStackTrace();
                }
            }


            /**
             * Getting posting line for query
             */
            String valueFromCorpus;
            String corpusPathAndLineDelim = "#";
            for (String specificTermKey: termInText.keySet()) {
                ArrayList<String> allTermsOfLetter = new ArrayList<>();
                if(corpusDictionary.containsKey(specificTermKey.toLowerCase())){
                    valueFromCorpus = corpusDictionary.get(specificTermKey.toLowerCase());
                }
                else{
                    valueFromCorpus = corpusDictionary.get(specificTermKey.toUpperCase());
                }

                if (valueFromCorpus != null) {
                    String[] splittedValue = valueFromCorpus.split(corpusPathAndLineDelim);
                    termFilePathTemp = Paths.get(splittedValue[0]);
                    try {
                        allTermsOfLetter = (ArrayList<String>) Files.readAllLines(termFilePathTemp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int lineNumberInFile = Integer.parseInt(valueFromCorpus.split(corpusPathAndLineDelim)[1]);
                    termswithPosting.put(specificTermKey, allTermsOfLetter.get(lineNumberInFile-1));

                    //System.out.println(allTermsOfLetter.get(lineNumberInFile-1));
                }
            }

            /**
             * Getting posting lines for description
             */
            valueFromCorpus = null;
            if(desc.getTextArray()!=null){
                for (String specificTermKey: descInText.keySet()) {
                    ArrayList<String> allTermsOfLetter = new ArrayList<>();
                    if(corpusDictionary.containsKey(specificTermKey.toLowerCase())){
                        valueFromCorpus = corpusDictionary.get(specificTermKey.toLowerCase());
                    }
                    else{
                        valueFromCorpus = corpusDictionary.get(specificTermKey.toUpperCase());
                    }

                    if (valueFromCorpus != null) {
                        String[] splittedValue = valueFromCorpus.split(corpusPathAndLineDelim);
                        termFilePathTemp = Paths.get(splittedValue[0]);
                        try {
                            allTermsOfLetter = (ArrayList<String>) Files.readAllLines(termFilePathTemp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int lineNumberInFile = Integer.parseInt(valueFromCorpus.split(corpusPathAndLineDelim)[1]);
                        termswithPostingDesc.put(specificTermKey, allTermsOfLetter.get(lineNumberInFile-1));

                        //System.out.println(allTermsOfLetter.get(lineNumberInFile-1));
                    }
                }
            }
            System.out.println("Finishing posting query: " +query.getDocNo());
            ranker.resetQuery();
            result =ranker.rankQueryDocs(termswithPosting,termInText,descInText,termswithPostingDesc,null,null);
            System.out.println("Finishing ranking query: " +query.getDocNo());
        }

       // result= Arrays.asList("doc1","doc2","doc3");

        return result;
    }
}
